package com.milko.wallet_service.dto.input;

import com.milko.wallet_service.dto.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTypeInputDto {
    private Integer id;
    private String name;
    private String currencyCode;
    private Status status;
    private String profileType;
    private String creator;
    private String modifier;
}
