package com.storemaster.repository;

import com.storemaster.domain.StoreWhitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoreWhitelistRepository extends JpaRepository<StoreWhitelist, Long> {

    @Query(value = "select w from StoreWhitelist w join fetch w.store s order by s.code",
            countQuery = "select count(w) from StoreWhitelist w join w.store s")
    Page<StoreWhitelist> findAllActive(Pageable pageable);

    @Query("select w from StoreWhitelist w join fetch w.store where w.id = :id")
    Optional<StoreWhitelist> findWithStore(@Param("id") Long id);

    @Query("select w.store.id from StoreWhitelist w where w.store.id in :storeIds")
    List<Long> findWhitelistedStoreIds(@Param("storeIds") Collection<Long> storeIds);

    boolean existsByStoreId(Long storeId);

    boolean existsByStoreIdAndIdNot(Long storeId, Long id);
}
