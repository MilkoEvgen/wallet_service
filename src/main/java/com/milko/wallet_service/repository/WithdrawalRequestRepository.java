package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.WithdrawalRequest;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public interface WithdrawalRequestRepository {
    WithdrawalRequest create(WithdrawalRequest request, DataSource dataSource);
    WithdrawalRequest findById(UUID requestId, DataSource dataSource);
}
