package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.output.WithdrawalRequestOutputDto;
import com.milko.wallet_service.model.WithdrawalRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WithdrawalRequestMapper {

    WithdrawalRequestOutputDto toWithdrawalRequestOutputDto(WithdrawalRequest request);
}
