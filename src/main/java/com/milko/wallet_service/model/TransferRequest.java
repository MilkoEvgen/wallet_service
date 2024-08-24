package com.milko.wallet_service.model;

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
public class TransferRequest {
    private UUID uuid;
    private LocalDateTime createdAt;
    private UUID paymentRequestUid;
    private UUID recipientUid;
    private UUID walletUidTo;
}
