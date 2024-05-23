package com.milko.wallet_service.repository;

import com.milko.wallet_service.model.WalletType;

import java.util.List;

public interface WalletTypeRepository {
    Integer create(WalletType walletType);
    WalletType findById(Integer id);
    List<WalletType> findAll();
    WalletType update(WalletType walletType);
    Boolean deleteById(Integer id);
}
