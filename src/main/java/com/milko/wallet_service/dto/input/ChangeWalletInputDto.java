package com.milko.wallet_service.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ChangeWalletInputDto {
    @JsonProperty("wallet_id")
    private UUID walletId;
    @JsonProperty("changed_by_user_id")
    private UUID changedByUserUid;
    @JsonProperty("changed_by_profile_type")
    private String changedByProfileType;
    private String reason;
    private String comment;
    @JsonProperty("to_status")
    private Status toStatus;
}
