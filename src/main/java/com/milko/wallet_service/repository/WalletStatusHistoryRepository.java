package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.WalletStatusHistory;
import com.milko.wallet_service.model.WalletTypeStatusHistory;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

public interface WalletStatusHistoryRepository {

    Boolean create(WalletStatusHistory walletStatusHistory, DataSource dataSource);
    void rollbackCreate(UUID uuid, DataSource dataSource);
    List<WalletStatusHistory> findAllByWalletId(UUID walletUid, DataSource dataSource);
}
