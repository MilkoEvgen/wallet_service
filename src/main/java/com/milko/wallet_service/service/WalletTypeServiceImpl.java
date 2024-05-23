package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.WalletTypeInputDto;
import com.milko.wallet_service.dto.WalletTypeOutputDto;
import com.milko.wallet_service.mapper.WalletTypeMapper;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.repository.WalletTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletTypeServiceImpl implements WalletTypeService{
    private final WalletTypeRepository walletTypeRepository;
    private final WalletTypeMapper walletTypeMapper;


    @Override
    public Integer create(WalletTypeInputDto walletTypeInputDto) {
        log.info("IN WalletTypeServiceImpl create(), walletTypeInputDto = {}", walletTypeInputDto);
        WalletType walletType = walletTypeMapper.toWalletType(walletTypeInputDto);
        return walletTypeRepository.create(walletType);
    }

    @Override
    public WalletTypeOutputDto findById(Integer id) {
        return walletTypeMapper.toWalletTypeOutputDto(walletTypeRepository.findById(id));
    }

    @Override
    public List<WalletTypeOutputDto> findAll() {
        return walletTypeRepository.findAll().stream()
                .map(walletTypeMapper::toWalletTypeOutputDto)
                .toList();
    }

    @Override
    public WalletTypeOutputDto update(WalletTypeInputDto walletTypeInputDto) {
        WalletType walletType = walletTypeMapper.toWalletType(walletTypeInputDto);
        return walletTypeMapper.toWalletTypeOutputDto(walletTypeRepository.update(walletType));
    }

    @Override
    public Boolean deleteById(Integer id) {
        return walletTypeRepository.deleteById(id);
    }
}
