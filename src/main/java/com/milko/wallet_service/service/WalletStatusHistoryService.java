package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.output.WalletStatusHistoryOutputDto;
import com.milko.wallet_service.model.WalletStatusHistory;

import java.util.List;
import java.util.UUID;

public interface WalletStatusHistoryService {

    void create(ChangeWalletInputDto changeWalletInputDto, Status fromStatus, UUID profileId);
    List<WalletStatusHistoryOutputDto> findAllByWalletId(UUID walletId, UUID profileId);
}
