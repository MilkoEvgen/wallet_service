package com.milko.wallet_service.service;

import com.milko.wallet_service.model.TransferRequest;

import java.util.UUID;

public interface TransferRequestService {
    TransferRequest create(TransferRequest request, UUID profileUid);
    TransferRequest findById(UUID uuid, UUID profileUid);
    TransferRequest findByPaymentRequestId(UUID paymentRequestId, UUID profileUid);
}
