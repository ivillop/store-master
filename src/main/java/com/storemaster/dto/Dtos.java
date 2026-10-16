package com.storemaster.dto;

import com.storemaster.domain.AuditLog;
import com.storemaster.domain.Branch;
import com.storemaster.domain.Store;
import com.storemaster.domain.StoreWhitelist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public final class Dtos {

    private Dtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
            return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                    page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    public record StoreResponse(Long id, String code, String name,
                                Long branchId, String branchCode, String branchName,
                                Long provinceId, String provinceName, boolean whitelisted) {
        public static StoreResponse from(Store s, boolean whitelisted) {
            Branch b = s.getBranch();
            return new StoreResponse(s.getId(), s.getCode(), s.getName(),
                    b.getId(), b.getCode(), b.getName(),
                    b.getProvince().getId(), b.getProvince().getName(), whitelisted);
        }
    }

    public record BranchResponse(Long id, String code, String name, Long provinceId, String provinceName,
                                 long version) {
        public static BranchResponse from(Branch b) {
            return new BranchResponse(b.getId(), b.getCode(), b.getName(),
                    b.getProvince().getId(), b.getProvince().getName(), b.getVersion());
        }
    }

    public record BranchUpdateRequest(@NotBlank @Size(max = 30) String code,
                                      @NotBlank @Size(max = 150) String name,
                                      @NotNull Long provinceId,
                                      @NotNull Long version) {
    }

    public record WhitelistRequest(@NotNull Long storeId, @Size(max = 255) String note) {
    }

    public record WhitelistUpdateRequest(@NotNull Long storeId, @Size(max = 255) String note,
                                         @NotNull Long version) {
    }

    public record WhitelistResponse(Long id, Long storeId, String storeCode, String storeName, String note,
                                    long version) {
        public static WhitelistResponse from(StoreWhitelist w) {
            return new WhitelistResponse(w.getId(), w.getStore().getId(), w.getStore().getCode(),
                    w.getStore().getName(), w.getNote(), w.getVersion());
        }
    }

    public record AuditLogResponse(Long id, String entityType, Long entityId, String action,
                                   String oldValue, String newValue, String username, Instant createdAt) {
        public static AuditLogResponse from(AuditLog a) {
            return new AuditLogResponse(a.getId(), a.getEntityType(), a.getEntityId(), a.getAction().name(),
                    a.getOldValue(), a.getNewValue(), a.getUsername(), a.getCreatedAt());
        }
    }
}
