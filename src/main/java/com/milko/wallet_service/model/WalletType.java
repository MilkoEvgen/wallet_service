package com.milko.wallet_service.model;

import com.milko.wallet_service.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletType {
    private UUID uuid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String name;
    private String currencyCode;
    private Status status;
    private LocalDateTime archivedAt;
    private String profileType;
    private String creator;
    private String modifier;
}

