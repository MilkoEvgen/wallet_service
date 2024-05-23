package com.milko.wallet_service.model;

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
public class Transaction {
    private UUID uid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private UUID linkedTransaction;
    private UUID profileUid;
    private UUID walletUid;
    private String walletName;
    private BigDecimal balanceOperationAmount;
    private BigDecimal rawAmount;
    private BigDecimal fee;
    private BigDecimal amountInUsd;
    private String type;
    private String state;
    private UUID paymentRequestUid;
    private String currencyCode;
    private Long refundFee;
}
