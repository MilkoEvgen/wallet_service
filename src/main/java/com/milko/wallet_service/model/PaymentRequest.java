package com.milko.wallet_service.model;

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
public class PaymentRequest {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private UUID profileUid;
    private UUID ownerWalletUid;
    private BigDecimal amount;
    private BigDecimal fee;
    private RequestType type;
    private Status status;
}
