package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.TransferRequest;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public interface TransferRequestRepository {
    TransferRequest create(TransferRequest request, DataSource dataSource);
    TransferRequest findById(UUID uuid, DataSource dataSource);
    TransferRequest findByPaymentRequestId(UUID paymentRequestId, DataSource dataSource);
}
