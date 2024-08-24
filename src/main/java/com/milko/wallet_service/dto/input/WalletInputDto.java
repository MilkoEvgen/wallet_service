package com.milko.wallet_service.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.milko.wallet_service.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletInputDto {
    private UUID uuid;
    private String name;
    @JsonProperty("wallet_type_id")
    private Integer walletTypeId;
    @JsonProperty("profile_uid")
    private UUID profileUid;
    private Status status;
    private BigDecimal balance;
}
