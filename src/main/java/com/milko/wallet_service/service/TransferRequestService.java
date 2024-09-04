package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.model.TransferRequest;

import java.util.UUID;

public interface TransferRequestService {
    TransferRequestOutputDto create(TransferRequest request, UUID profileUid);
    TransferRequestOutputDto findById(UUID uuid, UUID profileUid);
    TransferRequestOutputDto findByPaymentRequestId(UUID paymentRequestId, UUID profileUid);
}
