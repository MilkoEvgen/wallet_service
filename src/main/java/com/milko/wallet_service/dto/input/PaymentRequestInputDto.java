package com.milko.wallet_service.dto.input;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.milko.wallet_service.dto.RequestType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentRequestInputDto {
    private UUID profileUid;
    private UUID ownerWalletUid;
    private UUID recipientUid;
    private UUID walletUidTo;
    private BigDecimal amount;
    private RequestType type;
}
