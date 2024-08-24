package com.milko.wallet_service.dto.output;

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
public class WalletOutputDto {
    private UUID uuid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String name;
    private WalletTypeOutputDto walletType;
    private UUID profileUid;
    private Status status;
    private BigDecimal balance;
    private LocalDateTime archivedAt;
}
