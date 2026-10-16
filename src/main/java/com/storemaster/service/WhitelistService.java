package com.storemaster.service;

import com.storemaster.common.ApiException;
import com.storemaster.domain.AuditAction;
import com.storemaster.domain.Store;
import com.storemaster.domain.StoreWhitelist;
import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.dto.Dtos.WhitelistRequest;
import com.storemaster.dto.Dtos.WhitelistResponse;
import com.storemaster.dto.Dtos.WhitelistUpdateRequest;
import com.storemaster.repository.StoreRepository;
import com.storemaster.repository.StoreWhitelistRepository;
import com.storemaster.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhitelistService {

    private static final String ENTITY = "STORE_WHITELIST";

    private final StoreWhitelistRepository whitelist;
    private final StoreRepository stores;
    private final AuditService audit;
    private final PageRequests pageRequests;

    @Transactional(readOnly = true)
    public PageResponse<WhitelistResponse> list(Integer page, Integer size) {
        return PageResponse.from(whitelist.findAllActive(pageRequests.of(page, size)), WhitelistResponse::from);
    }

    @Transactional
    public WhitelistResponse create(WhitelistRequest request) {
        Store store = findStore(request.storeId());
        if (whitelist.existsByStoreId(store.getId())) {
            throw ApiException.conflict("Store " + store.getCode() + " is already whitelisted");
        }
        StoreWhitelist entry = new StoreWhitelist();
        entry.setStore(store);
        entry.setNote(request.note());
        whitelist.saveAndFlush(entry);

        audit.record(ENTITY, entry.getId(), AuditAction.CREATE, null, snapshot(entry));
        return WhitelistResponse.from(entry);
    }

    @Transactional
    public WhitelistResponse update(Long id, WhitelistUpdateRequest request) {
        StoreWhitelist entry = find(id);
        if (request.version() != entry.getVersion()) {
            throw ApiException.conflict("Whitelist entry was modified by someone else (current version "
                    + entry.getVersion() + "). Reload and try again.");
        }
        Map<String, Object> before = snapshot(entry);
        if (!entry.getStore().getId().equals(request.storeId())) {
            Store store = findStore(request.storeId());
            if (whitelist.existsByStoreIdAndIdNot(store.getId(), id)) {
                throw ApiException.conflict("Store " + store.getCode() + " is already whitelisted");
            }
            entry.setStore(store);
        }
        entry.setNote(request.note());
        whitelist.saveAndFlush(entry);

        audit.record(ENTITY, id, AuditAction.UPDATE, before, snapshot(entry));
        return WhitelistResponse.from(entry);
    }

    @Transactional
    public void delete(Long id) {
        StoreWhitelist entry = find(id);
        Map<String, Object> before = snapshot(entry);
        entry.markDeleted(CurrentUser.name(), Instant.now());
        whitelist.saveAndFlush(entry);
        audit.record(ENTITY, id, AuditAction.DELETE, before, null);
    }

    private StoreWhitelist find(Long id) {
        return whitelist.findWithStore(id).orElseThrow(() -> ApiException.notFound("Whitelist entry", id));
    }

    private Store findStore(Long storeId) {
        return stores.findById(storeId).orElseThrow(() -> ApiException.notFound("Store", storeId));
    }

    private static Map<String, Object> snapshot(StoreWhitelist w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("storeId", w.getStore().getId());
        m.put("note", w.getNote());
        m.put("version", w.getVersion());
        return m;
    }
}
