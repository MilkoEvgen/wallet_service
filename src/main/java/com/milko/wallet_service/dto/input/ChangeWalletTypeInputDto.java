package com.milko.wallet_service.dto.input;

import com.milko.wallet_service.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeWalletTypeInputDto {
    private UUID walletTypeId;
    private UUID changedByUserUid;
    private String changedByProfileType;
    private String reason;
    private String comment;
    private Status toStatus;
}
