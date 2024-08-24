package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.output.WalletStatusHistoryOutputDto;
import com.milko.wallet_service.model.WalletStatusHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletStatusHistoryMapper {

    WalletStatusHistoryOutputDto toWalletStatusHistoryOutputDto(WalletStatusHistory walletStatusHistory);

    List<WalletStatusHistoryOutputDto> toWalletStatusHistoryOutputDtoList(List<WalletStatusHistory> walletStatusHistories);
}
