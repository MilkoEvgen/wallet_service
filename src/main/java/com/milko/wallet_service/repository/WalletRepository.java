package com.milko.wallet_service.repository;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.Wallet;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Wallet create(Wallet wallet, DataSource dataSource);
    Optional<Wallet> findById(UUID walletId, DataSource dataSource);
    List<Wallet> findAllByUserId(UUID uuid, DataSource dataSource);
    Optional<Wallet> update(Wallet wallet, DataSource dataSource);
    Boolean updateBalance(UUID walletId, BigDecimal newBalance, DataSource dataSource);
    Boolean updateStatus(UUID walletId, Status status, DataSource dataSource);
    Boolean deleteById(UUID walletId, DataSource dataSource);

}
