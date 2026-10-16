package com.storemaster.web;

import com.storemaster.dto.Dtos.AuditLogResponse;
import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public PageResponse<AuditLogResponse> search(@RequestParam(required = false) String entityType,
                                                 @RequestParam(required = false) Long entityId,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size) {
        return auditService.search(entityType, entityId, page, size);
    }
}
