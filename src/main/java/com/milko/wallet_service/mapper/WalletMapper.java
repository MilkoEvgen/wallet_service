package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    Wallet toWallet(WalletInputDto walletInputDto);

    @Mapping(target = "walletType", ignore = true)
    WalletOutputDto toWalletOutputDto(Wallet wallet);

    default WalletOutputDto toWalletOutputDtoWithWalletType(Wallet wallet, WalletTypeOutputDto walletTypeOutputDto) {
        WalletOutputDto walletOutputDto = toWalletOutputDto(wallet);
        walletOutputDto.setWalletType(walletTypeOutputDto);
        return walletOutputDto;
    }
}
