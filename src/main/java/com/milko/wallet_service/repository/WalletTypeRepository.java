package com.milko.wallet_service.repository;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletType;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WalletTypeRepository {
    Boolean create(WalletType walletType, DataSource dataSource);
    WalletType findById(UUID uuid, DataSource dataSource);
    List<WalletType> findAll(DataSource dataSource);
    Boolean updateStatus(Status status, UUID id, UUID changedByUserId, DataSource dataSource);
    Boolean deleteById(UUID uuid, DataSource dataSource);
}
