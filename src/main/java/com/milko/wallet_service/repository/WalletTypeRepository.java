package com.milko.wallet_service.repository;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletType;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WalletTypeRepository {
    Boolean create(WalletType walletType, DataSource dataSource);
    void rollbackCreate(Integer id, DataSource dataSource);
    WalletType findById(Integer id, DataSource dataSource);
    List<WalletType> findAll(DataSource dataSource);
    Boolean updateStatus(Status status, Integer id, UUID changedByUserId, DataSource dataSource);
    void rollbackUpdate(Status status, Integer id, LocalDateTime dateTime, String changedByUserId, DataSource dataSource);
    Boolean deleteById(Integer id, DataSource dataSource);
    void rollbackDeleteById(Integer id, DataSource dataSource);
    Integer getMaxId(DataSource dataSource);
    String getCurrentStatusByWalletId(Integer id, DataSource dataSource);
}
