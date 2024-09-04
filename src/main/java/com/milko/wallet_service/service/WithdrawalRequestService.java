package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.WithdrawalRequestOutputDto;

import java.util.UUID;

public interface WithdrawalRequestService {
    WithdrawalRequestOutputDto create(UUID paymentRequestUid, UUID profileUid);
    WithdrawalRequestOutputDto findById(UUID requestId, UUID profileUid);
}
