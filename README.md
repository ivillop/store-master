# Store Master API

REST API master data **Provinsi → Cabang → Toko** dengan pencarian toko per provinsi, whitelist toko, autentikasi JWT, dan audit log.

**Stack:** Java 21, Spring Boot 3.5, Spring Data JPA (Hibernate), Spring Security + JWT, MySQL 8, Flyway, Maven.

## Prasyarat

- JDK 21 dan Maven 3.9+
- MySQL 8 yang berjalan di `localhost:3306` (user `root`, password `root`, atau atur lewat `DB_USERNAME`/`DB_PASSWORD`).

## Menjalankan

```bash
# 1. Pastikan MySQL lokal menyala (database store_master dibuat otomatis).

# 2. Jalankan aplikasi. Flyway membuat skema + 38 provinsi, lalu admin awal dibuat otomatis.
#    Profile "seed" mengisi data uji: 120 cabang dan 20.000 toko (hanya jalan bila tabel store masih kosong).
mvn spring-boot:run -Dspring-boot.run.profiles=seed
```

Aplikasi berjalan di `http://localhost:8080`. Akun awal: `admin` / `admin123` (ubah lewat env `ADMIN_USERNAME` / `ADMIN_PASSWORD`).

### Mencoba API

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')

# cari toko per provinsi (sudah termasuk toko whitelist)
curl -s "localhost:8080/api/stores?province=Jawa%20Barat&page=0&size=5" -H "Authorization: Bearer $TOKEN"

# tanpa token -> 401
curl -i "localhost:8080/api/stores?province=Jawa%20Barat"
```

Daftar endpoint lengkap dengan contoh request/response ada di [`docs/API.md`](docs/API.md), rancangan database dan ERD di [`docs/DATABASE.md`](docs/DATABASE.md).

## Menjalankan test

```bash
mvn test
```

Test memakai MySQL lokal yang sama, pada database terpisah `store_master_test` yang dibuat otomatis. Koneksi bisa diubah lewat `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`. Tiap test berjalan dalam transaksi yang di-rollback, jadi tidak ada data sisa.

Bila muncul error `You do not have the SUPER privilege and binary logging is enabled` saat migrasi, jalankan sekali: `SET GLOBAL log_bin_trust_function_creators = 1;` (atau gunakan user `root`). Ini diperlukan MySQL untuk membuat trigger audit log.

- `LikePatternTest`: unit test escape pola pencarian.
- `StoreMasterIntegrationTest`: integration test end-to-end terhadap MySQL asli. Mencakup 401 tanpa token, pencarian + whitelist lintas provinsi, soft delete, optimistic locking (409), CRUD whitelist, audit log, dan audit log yang append-only.

## Konfigurasi (`application.yml` / environment variable)

| Properti                               | Default | Fungsi                                                           |
| -------------------------------------- | ------- | ---------------------------------------------------------------- |
| `app.paging.default-size`              | 20      | Ukuran halaman bila klien tidak mengirim `size`                  |
| `app.paging.max-size`                  | 100     | Batas atas `size` (melindungi performa)                          |
| `app.search.province-match`            | `EXACT` | `EXACT` atau `CONTAINS` untuk pencarian nama provinsi            |
| `app.search.include-whitelist`         | `true`  | Toko whitelist selalu tampil di semua provinsi                   |
| `app.jwt.secret` / `JWT_SECRET`        | (dev)   | Kunci HMAC, minimal 32 karakter. **Wajib diganti di production** |
| `app.jwt.expiration`                   | `2h`    | Masa berlaku token                                               |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | lokal   | Koneksi MySQL                                                    |

Mengubah aturan di atas tidak memerlukan perubahan kode.

## Keputusan desain

- **Hanya data aktif:** semua entity memakai soft delete (`deleted_at`) dan `@SQLRestriction("deleted_at IS NULL")`, sehingga setiap query dan join otomatis menyaring data terhapus. Menghapus cabang ikut menghapus (soft) toko di bawahnya.
- **Skala besar:** pagination wajib dengan batas maksimum, index pada FK dan nama provinsi, satu query join (tanpa N+1), dan pola `LIKE` tanpa wildcard di depan agar index terpakai. Seeder 20.000 toko dibuat dengan JDBC batch.
- **Whitelist:** hasil pencarian = toko di provinsi tersebut ∪ toko whitelist aktif, tanpa duplikat, dalam satu query. Setiap toko hanya bisa masuk whitelist sekali (unique pada data aktif).
- **Unique key aman untuk soft delete:** kolom generated `active_key` membuat kode/nama hanya unik di antara data aktif, sehingga kode yang sudah dihapus boleh dipakai lagi.
- **Keamanan dan integritas:** JWT stateless, password BCrypt, status user dicek ke database di tiap request, validasi input, optimistic locking (`version`, 409 bila bentrok), dan audit log append-only yang dijaga trigger database.
- **Audit log:** setiap create/update/delete mencatat siapa, kapan, nilai lama, dan nilai baru dalam transaksi yang sama dengan perubahan datanya. Bisa ditelusuri lewat `GET /api/audit-logs`.

## Catatan

- Gunakan `-Dspring-boot.run.profiles=seed` hanya untuk pengembangan/uji performa.
- Pagination memakai offset. Bila data tumbuh jauh di atas ratusan ribu baris dan halaman dalam sering diakses, ganti ke keyset pagination (`code > :lastCode`).
