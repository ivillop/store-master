package com.storemaster.web;

import com.storemaster.dto.Dtos.PageResponse;
import com.storemaster.dto.Dtos.WhitelistRequest;
import com.storemaster.dto.Dtos.WhitelistResponse;
import com.storemaster.dto.Dtos.WhitelistUpdateRequest;
import com.storemaster.service.WhitelistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whitelist")
@RequiredArgsConstructor
public class WhitelistController {

    private final WhitelistService whitelistService;

    @GetMapping
    public PageResponse<WhitelistResponse> list(@RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size) {
        return whitelistService.list(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WhitelistResponse create(@Valid @RequestBody WhitelistRequest request) {
        return whitelistService.create(request);
    }

    @PutMapping("/{id}")
    public WhitelistResponse update(@PathVariable Long id, @Valid @RequestBody WhitelistUpdateRequest request) {
        return whitelistService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        whitelistService.delete(id);
    }
}
