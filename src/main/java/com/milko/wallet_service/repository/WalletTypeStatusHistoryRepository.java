package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.WalletTypeStatusHistory;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

public interface WalletTypeStatusHistoryRepository {
    Boolean create(WalletTypeStatusHistory walletTypeStatusHistory, DataSource dataSource);
    void rollbackCreate(Long id, DataSource dataSource);
    List<WalletTypeStatusHistory> findAllByWalletTypeId(UUID walletTypeId, DataSource dataSource);
    Long getMaxId(DataSource dataSource);
}
