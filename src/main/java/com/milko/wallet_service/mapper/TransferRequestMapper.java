package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.model.TransferRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferRequestMapper {
    TransferRequestOutputDto toTransferRequestOutputDto(TransferRequest request);
}
