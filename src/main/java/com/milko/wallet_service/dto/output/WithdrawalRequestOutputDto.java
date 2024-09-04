package com.milko.wallet_service.dto.output;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class WithdrawalRequestOutputDto {
    private UUID uuid;
    private LocalDateTime createdAt;
    private UUID paymentRequestUid;
}
