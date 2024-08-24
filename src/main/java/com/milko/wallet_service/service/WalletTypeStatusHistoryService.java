package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletTypeStatusHistory;

import javax.sql.DataSource;
import java.util.List;

public interface WalletTypeStatusHistoryService {

    void create(ChangeWalletTypeInputDto changeWalletTypeInputDto, Long generatedId, Status fromStatus, DataSource dataSource);
    void rollbackCreate(Long id, DataSource dataSource);
    List<WalletTypeStatusHistory> findAllByWalletTypeId(Integer walletTypeId);
    Long getMaxId(DataSource dataSource);

}
