package com.storemaster.repository;

import com.storemaster.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(value = """
            select s from Store s join fetch s.branch b join fetch b.province p
            where p.name like :pattern escape '!'
               or s.id in (select w.store.id from StoreWhitelist w)
            order by s.code""",
            countQuery = """
                    select count(s) from Store s join s.branch b join b.province p
                    where p.name like :pattern escape '!'
                       or s.id in (select w.store.id from StoreWhitelist w)""")
    Page<Store> searchByProvinceWithWhitelist(@Param("pattern") String pattern, Pageable pageable);

    @Query(value = """
            select s from Store s join fetch s.branch b join fetch b.province p
            where p.name like :pattern escape '!'
            order by s.code""",
            countQuery = """
                    select count(s) from Store s join s.branch b join b.province p
                    where p.name like :pattern escape '!'""")
    Page<Store> searchByProvince(@Param("pattern") String pattern, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Store s set s.deletedAt = :at, s.deletedBy = :by, s.updatedAt = :at, s.updatedBy = :by,
                   s.version = s.version + 1
            where s.branch.id = :branchId and s.deletedAt is null""")
    int softDeleteByBranch(@Param("branchId") Long branchId, @Param("at") Instant at, @Param("by") String by);
}
