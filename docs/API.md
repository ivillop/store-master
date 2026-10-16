# Dokumentasi API

Base URL: `http://localhost:8080`. Semua endpoint selain `POST /api/auth/login` dan `/actuator/health` wajib header `Authorization: Bearer <token>`. Body dan response berformat JSON.

## Format error

```json
{ "timestamp": "2026-10-15T03:00:00Z", "status": 400, "message": "Validation failed",
  "errors": { "name": "must not be blank" } }
```

| Status | Arti |
|---|---|
| 400 | Input tidak valid atau parameter wajib tidak ada |
| 401 | Token tidak ada, salah, atau kedaluwarsa; login gagal |
| 404 | Data tidak ditemukan (termasuk data yang sudah dihapus) |
| 409 | Konflik: versi data usang, kode duplikat, atau toko sudah di whitelist |

## Response berhalaman

```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 135, "totalPages": 7 }
```

Query `page` (mulai 0) dan `size` (maksimum sesuai `app.paging.max-size`) bersifat opsional pada semua endpoint daftar.

---

## 1. Login

`POST /api/auth/login`

```json
{ "username": "admin", "password": "admin123" }
```

Response `200`:

```json
{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", "expiresInSeconds": 7200 }
```

## 2. Cari toko berdasarkan provinsi

`GET /api/stores?province={nama}&page=0&size=20`

Mengembalikan toko aktif di provinsi tersebut **ditambah** semua toko whitelist aktif (muncul di semua provinsi). `province` wajib. Pencocokan tidak membedakan huruf besar/kecil; mode `EXACT`/`CONTAINS` diatur di konfigurasi.

Response `200`:

```json
{
  "content": [
    { "id": 12, "code": "ST-000012", "name": "Toko 12",
      "branchId": 12, "branchCode": "BR-0012", "branchName": "Cabang 12",
      "provinceId": 12, "provinceName": "Jawa Barat", "whitelisted": false }
  ],
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
}
```

## 3. Cabang

| Method | Path | Fungsi |
|---|---|---|
| GET | `/api/branches?provinceId=&page=&size=` | Daftar cabang aktif (filter provinsi opsional) |
| GET | `/api/branches/{id}` | Detail cabang |
| PUT | `/api/branches/{id}` | Perbarui cabang |
| DELETE | `/api/branches/{id}` | Hapus cabang (soft delete, toko di bawahnya ikut dihapus) |

`PUT /api/branches/12`

```json
{ "code": "BR-0012", "name": "Cabang Bandung Kota", "provinceId": 12, "version": 0 }
```

Response `200`:

```json
{ "id": 12, "code": "BR-0012", "name": "Cabang Bandung Kota", "provinceId": 12,
  "provinceName": "Jawa Barat", "version": 1 }
```

`version` wajib diisi dengan versi terakhir yang dibaca klien. Bila data sudah diubah pihak lain, API membalas `409`. `DELETE` membalas `204` tanpa body.

## 4. Whitelist toko

| Method | Path | Fungsi |
|---|---|---|
| GET | `/api/whitelist` | Daftar whitelist aktif |
| POST | `/api/whitelist` | Tambah toko ke whitelist (`201`) |
| PUT | `/api/whitelist/{id}` | Ubah toko/catatan whitelist |
| DELETE | `/api/whitelist/{id}` | Hapus dari whitelist (`204`) |

`POST /api/whitelist`

```json
{ "storeId": 345, "note": "Toko flagship" }
```

Response `201`:

```json
{ "id": 1, "storeId": 345, "storeCode": "ST-000345", "storeName": "Toko 345",
  "note": "Toko flagship", "version": 0 }
```

`PUT /api/whitelist/1`

```json
{ "storeId": 345, "note": "Catatan baru", "version": 0 }
```

Menambahkan toko yang sudah ada di whitelist membalas `409`.

## 5. Audit log (read-only)

`GET /api/audit-logs?entityType=BRANCH&entityId=12&page=0&size=20`

```json
{ "content": [ { "id": 5, "entityType": "BRANCH", "entityId": 12, "action": "UPDATE",
    "oldValue": "{\"id\":12,\"code\":\"BR-0012\",\"name\":\"Cabang 12\",\"provinceId\":12,\"version\":0}",
    "newValue": "{\"id\":12,\"code\":\"BR-0012\",\"name\":\"Cabang Bandung Kota\",\"provinceId\":12,\"version\":1}",
    "username": "admin", "createdAt": "2026-10-15T03:05:00Z" } ],
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```

`entityType`: `BRANCH` atau `STORE_WHITELIST`; `action`: `CREATE`, `UPDATE`, `DELETE`. Hasil diurutkan dari yang terbaru.
