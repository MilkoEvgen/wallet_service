package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.TopUpRequest;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public interface TopUpRequestRepository {
    TopUpRequest create(TopUpRequest request, DataSource dataSource);
    Optional<TopUpRequest> findById(UUID id, DataSource dataSource);
    Optional<TopUpRequest> findByPaymentRequestId(UUID paymentRequestId, DataSource dataSource);
}
