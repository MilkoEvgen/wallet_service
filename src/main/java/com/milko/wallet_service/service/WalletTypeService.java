package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;

import java.util.List;

public interface WalletTypeService {
    Integer create(WalletTypeInputDto walletTypeInputDto);
    WalletTypeOutputDto findById(Integer id);
    List<WalletTypeOutputDto> findAll();
    WalletTypeOutputDto update(ChangeWalletTypeInputDto changeWalletTypeInputDto);
    Boolean deleteById(Integer id);
    String getCurrentStatusByWalletId(Integer id);
}
