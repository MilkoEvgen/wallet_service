package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletTypeStatusHistory;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

public interface WalletTypeStatusHistoryService {

    void create(ChangeWalletTypeInputDto changeWalletTypeInputDto, UUID generatedId, Status fromStatus, DataSource dataSource);
    List<WalletTypeStatusHistory> findAllByWalletTypeId(UUID walletTypeId);

}
