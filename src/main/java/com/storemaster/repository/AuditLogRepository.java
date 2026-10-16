package com.storemaster.repository;

import com.storemaster.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query(value = """
            select a from AuditLog a
            where (:entityType is null or a.entityType = :entityType)
              and (:entityId is null or a.entityId = :entityId)
            order by a.id desc""",
            countQuery = """
                    select count(a) from AuditLog a
                    where (:entityType is null or a.entityType = :entityType)
                      and (:entityId is null or a.entityId = :entityId)""")
    Page<AuditLog> search(@Param("entityType") String entityType, @Param("entityId") Long entityId,
                          Pageable pageable);
}
