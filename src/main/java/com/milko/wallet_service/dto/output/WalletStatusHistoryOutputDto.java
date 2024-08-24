package com.milko.wallet_service.dto.output;

import com.milko.wallet_service.dto.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class WalletStatusHistoryOutputDto {
    private UUID uuid;
    private LocalDateTime createdAt;
    private UUID walletUid;
    private UUID changedByUserUid;
    private String changedByProfileType;
    private String reason;
    private Status fromStatus;
    private String comment;
    private Status toStatus;
}
