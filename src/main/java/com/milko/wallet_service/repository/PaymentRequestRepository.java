package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.PaymentRequest;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRequestRepository {

    PaymentRequest create(PaymentRequest request, DataSource dataSource);
    Optional<PaymentRequest> findById(UUID requestId, DataSource dataSource);
    List<PaymentRequest> findAllByUserId(UUID profileId, DataSource dataSource);
}
