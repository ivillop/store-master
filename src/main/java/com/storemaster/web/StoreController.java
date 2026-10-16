package com.storemaster.web;

import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.dto.Dtos.StoreResponse;
import com.storemaster.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public PageResponse<StoreResponse> search(@RequestParam(required = false) String province,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size) {
        return storeService.searchByProvince(province, page, size);
    }
}
