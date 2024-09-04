package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {
    WalletOutputDto create(WalletInputDto wallet);
    Boolean topUp(UUID walletUid, BigDecimal amount, UUID profileId);
    Boolean withdraw(UUID walletUid, BigDecimal amount, UUID profileId);
    WalletOutputDto findById(UUID walletId, UUID profileId);
    List<WalletOutputDto> findAllByProfileId(UUID uuid);
    WalletOutputDto updateStatus(ChangeWalletInputDto changeWalletInputDto);
    Boolean deleteById(UUID walletId, UUID profileId);
}
