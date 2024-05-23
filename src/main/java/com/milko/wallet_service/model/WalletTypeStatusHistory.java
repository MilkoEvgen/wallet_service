package com.milko.wallet_service.model;

import com.milko.wallet_service.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTypeStatusHistory {
    private Long id;
    private LocalDateTime createdAt;
    private Integer walletTypeId;
    private UUID changedByUserUid;
    private String changedByProfileType;
    private String reason;
    private Status fromStatus;
    private String comment;
    private Status toStatus;

}
