package com.milko.wallet_service.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class WalletRequestInputDto {
    @JsonProperty("wallet_id")
    private UUID walletId;
    @JsonProperty("profile_id")
    private UUID profileId;
}
