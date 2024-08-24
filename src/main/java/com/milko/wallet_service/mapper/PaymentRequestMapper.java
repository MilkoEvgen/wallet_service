package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.model.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentRequestMapper {

    PaymentRequest toPaymentRequest(PaymentRequestInputDto dto);
    PaymentRequestOutputDto toPaymentRequestOutputDto(PaymentRequest request);
    List<PaymentRequestOutputDto> toPaymentRequestOutputDtoList(List<PaymentRequest> requests);
}
