package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.WalletTypeInputDto;
import com.milko.wallet_service.dto.WalletTypeOutputDto;
import com.milko.wallet_service.model.WalletType;

import java.util.List;

public interface WalletTypeService {
    Integer create(WalletTypeInputDto walletTypeInputDto);
    WalletTypeOutputDto findById(Integer id);
    List<WalletTypeOutputDto> findAll();
    WalletTypeOutputDto update(WalletTypeInputDto walletTypeInputDto);
    Boolean deleteById(Integer id);
}
