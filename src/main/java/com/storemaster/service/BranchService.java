package com.storemaster.service;

import com.storemaster.common.ApiException;
import com.storemaster.domain.AuditAction;
import com.storemaster.domain.Branch;
import com.storemaster.domain.Province;
import com.storemaster.dto.Dtos.BranchResponse;
import com.storemaster.dto.Dtos.BranchUpdateRequest;
import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.repository.BranchRepository;
import com.storemaster.repository.ProvinceRepository;
import com.storemaster.repository.StoreRepository;
import com.storemaster.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BranchService {

    private static final String ENTITY = "BRANCH";

    private final BranchRepository branches;
    private final ProvinceRepository provinces;
    private final StoreRepository stores;
    private final AuditService audit;
    private final PageRequests pageRequests;

    @Transactional(readOnly = true)
    public PageResponse<BranchResponse> list(Long provinceId, Integer page, Integer size) {
        return PageResponse.from(branches.search(provinceId, pageRequests.of(page, size)), BranchResponse::from);
    }

    @Transactional(readOnly = true)
    public BranchResponse get(Long id) {
        return BranchResponse.from(find(id));
    }

    @Transactional
    public BranchResponse update(Long id, BranchUpdateRequest request) {
        Branch branch = find(id);
        if (request.version() != branch.getVersion()) {
            throw ApiException.conflict("Branch was modified by someone else (current version "
                    + branch.getVersion() + "). Reload and try again.");
        }
        String code = request.code().trim();
        if (branches.existsByCodeAndIdNot(code, id)) {
            throw ApiException.conflict("Branch code '" + code + "' is already used");
        }
        Province province = provinces.findById(request.provinceId())
                .orElseThrow(() -> ApiException.notFound("Province", request.provinceId()));

        Map<String, Object> before = snapshot(branch);
        branch.setCode(code);
        branch.setName(request.name().trim());
        branch.setProvince(province);
        branches.saveAndFlush(branch);  

        audit.record(ENTITY, id, AuditAction.UPDATE, before, snapshot(branch));
        return BranchResponse.from(branch);
    }

    @Transactional
    public void delete(Long id) {
        Branch branch = find(id);
        Map<String, Object> before = snapshot(branch);
        Instant now = Instant.now();
        String by = CurrentUser.name();

        branch.markDeleted(by, now);
        branches.saveAndFlush(branch);
        int deletedStores = stores.softDeleteByBranch(id, now, by);

        audit.record(ENTITY, id, AuditAction.DELETE, before, Map.of("deletedStores", deletedStores));
    }

    private Branch find(Long id) {
        return branches.findWithProvince(id).orElseThrow(() -> ApiException.notFound("Branch", id));
    }

    private static Map<String, Object> snapshot(Branch b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("code", b.getCode());
        m.put("name", b.getName());
        m.put("provinceId", b.getProvince().getId());
        m.put("version", b.getVersion());
        return m;
    }
}
