package com.storemaster.service;

import com.storemaster.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageRequests {

    private final AppProperties props;

    public Pageable of(Integer page, Integer size) {
        int max = props.paging().maxSize();
        int s = (size == null || size < 1) ? props.paging().defaultSize() : Math.min(size, max);
        int p = (page == null || page < 0) ? 0 : page;
        return PageRequest.of(p, s);
    }
}
