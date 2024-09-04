package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.TopUpRequestOutputDto;

import java.util.UUID;

public interface TopUpRequestService {
    TopUpRequestOutputDto create(UUID paymentRequestUid, UUID profileUid);
    TopUpRequestOutputDto findById(UUID requestId, UUID profileUid);
}
