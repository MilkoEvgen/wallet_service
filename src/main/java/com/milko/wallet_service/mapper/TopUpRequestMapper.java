package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.output.TopUpRequestOutputDto;
import com.milko.wallet_service.model.TopUpRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TopUpRequestMapper {
    TopUpRequestOutputDto toTopUpRequestOutputDto(TopUpRequest request);
}
