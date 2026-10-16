package com.storemaster.service;

import com.storemaster.common.ApiException;
import com.storemaster.config.AppProperties;
import com.storemaster.domain.Store;
import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.dto.Dtos.StoreResponse;
import com.storemaster.repository.StoreRepository;
import com.storemaster.repository.StoreWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository stores;
    private final StoreWhitelistRepository whitelist;
    private final AppProperties props;
    private final PageRequests pageRequests;

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> searchByProvince(String province, Integer page, Integer size) {
        if (province == null || province.isBlank()) {
            throw ApiException.badRequest("Query parameter 'province' is required");
        }
        Pageable pageable = pageRequests.of(page, size);
        String pattern = LikePattern.of(province.trim(), props.search().provinceMatch());

        Page<Store> result = props.search().includeWhitelist()
                ? stores.searchByProvinceWithWhitelist(pattern, pageable)
                : stores.searchByProvince(pattern, pageable);

        List<Long> ids = result.getContent().stream().map(Store::getId).toList();
        Set<Long> whitelisted = ids.isEmpty() ? Set.of() : new HashSet<>(whitelist.findWhitelistedStoreIds(ids));

        return PageResponse.from(result, s -> StoreResponse.from(s, whitelisted.contains(s.getId())));
    }
}
