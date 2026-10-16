package com.storemaster.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storemaster.domain.AuditAction;
import com.storemaster.domain.AuditLog;
import com.storemaster.dto.Dtos.AuditLogResponse;
import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.repository.AuditLogRepository;
import com.storemaster.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;
    private final ObjectMapper mapper;
    private final PageRequests pageRequests;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String entityType, Long entityId, AuditAction action, Object oldValue, Object newValue) {
        repository.save(new AuditLog(entityType, entityId, action, toJson(oldValue), toJson(newValue),
                CurrentUser.name()));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(String entityType, Long entityId, Integer page, Integer size) {
        String type = (entityType == null || entityType.isBlank()) ? null : entityType.trim().toUpperCase();
        return PageResponse.from(repository.search(type, entityId, pageRequests.of(page, size)),
                AuditLogResponse::from);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize audit payload", e);
        }
    }
}
