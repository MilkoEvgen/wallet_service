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
public class PaymentRequest {
    private UUID uuid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private UUID profileUid;
    private UUID walletUid;
    private BigDecimal amountGross;
    private BigDecimal fee;
    private Status status;
    private BigDecimal percentage;
    private BigDecimal fixedAmount;
    private String option;
    private Integer scale;
    private String comment;
    private UUID providerTransactionUid;
    private String providerTransactionId;
    private Long paymentMethodId;
}
