package com.storemaster.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Value("${app.seed.branches:120}")
    private int branchCount;
    @Value("${app.seed.stores:20000}")
    private int storeCount;

    @Override
    public void run(ApplicationArguments args) {
        Long existing = jdbc.queryForObject("select count(*) from store", Long.class);
        if (existing != null && existing > 0) {
            log.info("Seeder dilewati: tabel store sudah berisi data");
            return;
        }
        List<Long> provinceIds = jdbc.queryForList("select id from province order by id", Long.class);

        jdbc.batchUpdate("""
                        insert into branch (province_id, code, name, created_at, created_by, version)
                        values (?, ?, ?, UTC_TIMESTAMP(6), 'seed', 0)""",
                IntStream.rangeClosed(1, branchCount).boxed().toList(), 500, (ps, i) -> {
                    ps.setLong(1, provinceIds.get((i - 1) % provinceIds.size()));
                    ps.setString(2, String.format("BR-%04d", i));
                    ps.setString(3, "Cabang " + i);
                });

        List<Long> branchIds = jdbc.queryForList("select id from branch order by id", Long.class);

        jdbc.batchUpdate("""
                        insert into store (branch_id, code, name, created_at, created_by, version)
                        values (?, ?, ?, UTC_TIMESTAMP(6), 'seed', 0)""",
                IntStream.rangeClosed(1, storeCount).boxed().toList(), 1000, (ps, i) -> {
                    ps.setLong(1, branchIds.get((i - 1) % branchIds.size()));
                    ps.setString(2, String.format("ST-%06d", i));
                    ps.setString(3, "Toko " + i);
                });
        log.info("Seeder selesai: {} cabang, {} toko", branchCount, storeCount);
    }
}
