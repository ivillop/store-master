package com.storemaster.repository;

import com.storemaster.domain.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query(value = """
            select b from Branch b join fetch b.province p
            where (:provinceId is null or p.id = :provinceId)
            order by b.code""",
            countQuery = """
                    select count(b) from Branch b
                    where (:provinceId is null or b.province.id = :provinceId)""")
    Page<Branch> search(@Param("provinceId") Long provinceId, Pageable pageable);

    @Query("select b from Branch b join fetch b.province where b.id = :id")
    Optional<Branch> findWithProvince(@Param("id") Long id);

    boolean existsByCodeAndIdNot(String code, Long id);
}
