package com.milko.wallet_service.dto.input;

import com.milko.wallet_service.dto.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ChangeWalletTypeInputDto {
    private Integer walletTypeId;
    private UUID changedByUserUid;
    private String changedByProfileType;
    private String reason;
    private String comment;
    private Status toStatus;
}
