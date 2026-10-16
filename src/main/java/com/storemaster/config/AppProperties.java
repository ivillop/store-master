package com.storemaster.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid Paging paging, @Valid Search search, @Valid Jwt jwt, Bootstrap bootstrap) {

    public record Paging(@Min(1) int defaultSize, @Min(1) int maxSize) {
    }

    public record Search(@NotNull MatchMode provinceMatch, boolean includeWhitelist) {
    }

    public record Jwt(@NotBlank @Size(min = 32) String secret, @NotNull Duration expiration) {
    }

    public record Bootstrap(String adminUsername, String adminPassword) {
    }

    public enum MatchMode {EXACT, CONTAINS}
}
