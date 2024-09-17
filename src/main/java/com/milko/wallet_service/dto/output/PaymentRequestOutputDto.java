package com.milko.wallet_service.dto.output;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentRequestOutputDto {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private UUID profileUid;
    private UUID ownerWalletUid;
    private UUID recipientUid;
    private UUID walletUidTo;
    private BigDecimal amount;
    private BigDecimal fee;
    private RequestType type;
    private Status status;
}
