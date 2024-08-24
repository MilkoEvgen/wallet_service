package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.model.WalletType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletTypeMapper {
    WalletType toWalletType(WalletTypeInputDto walletTypeInputDto);

    WalletTypeOutputDto toWalletTypeOutputDto(WalletType walletType);
}
