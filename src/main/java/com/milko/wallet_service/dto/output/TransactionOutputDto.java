package com.milko.wallet_service.dto.output;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.milko.wallet_service.model.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionOutputDto {
    private UUID uuid;
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
    private TransactionStatus status;
}
