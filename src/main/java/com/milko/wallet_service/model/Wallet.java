package com.milko.wallet_service.model;

import com.milko.wallet_service.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    private UUID uuid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String name;
    private UUID walletTypeId;
    private UUID profileUid;
    private Status status;
    private BigDecimal balance;
    private LocalDateTime archivedAt;
}

