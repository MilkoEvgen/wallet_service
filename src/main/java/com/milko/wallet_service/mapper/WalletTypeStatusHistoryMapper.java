package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.output.WalletTypeStatusHistoryOutputDto;
import com.milko.wallet_service.model.WalletTypeStatusHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletTypeStatusHistoryMapper {

    WalletTypeStatusHistoryOutputDto toWalletTypeStatusHistoryOutputDto(WalletTypeStatusHistory walletTypeStatusHistory);
}
