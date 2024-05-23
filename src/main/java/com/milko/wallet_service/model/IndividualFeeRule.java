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
public class IndividualFeeRule {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String transactionType;
    private BigDecimal percentage;
    private BigDecimal fixedAmount;
    private String option;
    private LocalDateTime archivedAt;
    private UUID walletUid;
    private Boolean isAllWallets;
    private Integer walletTypeId;
    private Integer paymentMethodId;
    private Integer amountFrom;
    private Integer amountTo;
    private Boolean highestPriority;
    private String feeCurrency;
    private String feeRuleType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Status status;
    private UUID creatorProfileUid;
    private UUID modifierProfileUid;
}
