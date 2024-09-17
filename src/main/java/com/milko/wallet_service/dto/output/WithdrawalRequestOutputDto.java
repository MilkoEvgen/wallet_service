package com.milko.wallet_service.dto.output;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestOutputDto {
    private UUID uuid;
    private LocalDateTime createdAt;
    private UUID paymentRequestUid;
}
