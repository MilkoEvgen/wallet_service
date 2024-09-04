package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;

import java.util.List;
import java.util.UUID;

public interface WalletTypeService {
    UUID create(WalletTypeInputDto walletTypeInputDto);
    WalletTypeOutputDto findById(UUID uuid);
    List<WalletTypeOutputDto> findAll();
    WalletTypeOutputDto update(ChangeWalletTypeInputDto changeWalletTypeInputDto);
    Boolean deleteById(UUID uuid);
}
