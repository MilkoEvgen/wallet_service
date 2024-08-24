package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.model.PaymentRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentRequestService {

    PaymentRequestOutputDto create(PaymentRequestInputDto request);
    PaymentRequestOutputDto findById(UUID requestId, UUID profileId);
    List<PaymentRequestOutputDto> findAllByUserId(UUID profileId);
}
