package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.mapper.WalletMapper;
import com.milko.wallet_service.model.Wallet;
import com.milko.wallet_service.repository.WalletRepository;
import com.milko.wallet_service.service.WalletService;
import com.milko.wallet_service.service.WalletStatusHistoryService;
import com.milko.wallet_service.service.WalletTypeService;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final ShardService shardService;
    private final WalletRepository walletRepository;
    private final WalletStatusHistoryService walletStatusHistoryService;
    private final WalletTypeService walletTypeService;
    private final WalletMapper walletMapper;

    @Override
    public WalletOutputDto create(WalletInputDto walletInputDto) {
        log.info("IN create, wallet = {}", walletMapper.toWallet(walletInputDto));
        DataSource dataSource = shardService.getDataSourceByUuid(walletInputDto.getProfileUid());
        walletInputDto.setUuid(UUID.randomUUID());
        Wallet wallet = walletRepository.create(walletMapper.toWallet(walletInputDto), dataSource);
        WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(walletInputDto.getWalletTypeId());
        return walletMapper.toWalletOutputDtoWithWalletType(wallet, walletTypeOutputDto);
    }

    @Override
    public WalletOutputDto topUp(UUID walletUid, BigDecimal amount, UUID profileId) {
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        Optional<Wallet> optionalWallet = walletRepository.findById(walletUid, dataSource);
        Wallet wallet = optionalWallet.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        BigDecimal newBalance = wallet.getBalance().add(amount);
        walletRepository.updateBalance(walletUid, newBalance, dataSource);
        return null;
    }

    @Override
    public WalletOutputDto withdraw(UUID walletUid, BigDecimal amount, UUID profileId) {
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        Optional<Wallet> optionalWallet = walletRepository.findById(walletUid, dataSource);
        Wallet wallet = optionalWallet.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new LowBalanceException();
        }
        walletRepository.updateBalance(walletUid, newBalance, dataSource);
        return null;
    }

    @Override
    public WalletOutputDto findById(UUID walletId, UUID profileId) {
        log.info("IN findById, walletId = {}, profileId = {}", walletId, profileId);
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        Optional<Wallet> optionalWallet = walletRepository.findById(walletId, dataSource);
        Wallet wallet = optionalWallet.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(wallet.getWalletTypeId());
        return walletMapper.toWalletOutputDtoWithWalletType(wallet, walletTypeOutputDto);
    }

    @Override
    public List<WalletOutputDto> findAllByProfileId(UUID profileId) {
        log.info("IN findAllByProfileId, profileId = {}", profileId);
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        return walletRepository.findAllByUserId(profileId, dataSource).stream()
                .map(wallet -> {
                    WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(wallet.getWalletTypeId());
                    return walletMapper.toWalletOutputDtoWithWalletType(wallet, walletTypeOutputDto);
                }).collect(Collectors.toList());
    }

    @Override
    public WalletOutputDto updateStatus(ChangeWalletInputDto changeWalletInputDto) {
        log.info("IN updateStatus, changeWalletInputDto = {}", changeWalletInputDto);
        DataSource dataSource = shardService.getDataSourceByUuid(changeWalletInputDto.getChangedByUserUid());
        Optional<Wallet> optionalWallet = walletRepository.findById(changeWalletInputDto.getWalletId(), dataSource);
        Wallet wallet = optionalWallet.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        walletRepository.updateStatus(changeWalletInputDto.getWalletId(), changeWalletInputDto.getToStatus(), dataSource);
        walletStatusHistoryService.create(changeWalletInputDto, wallet.getStatus(), changeWalletInputDto.getChangedByUserUid());
        Optional<Wallet> updatedOptionalWallet = walletRepository.findById(changeWalletInputDto.getWalletId(), dataSource);
        Wallet updatedWallet = updatedOptionalWallet.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(wallet.getWalletTypeId());
        return walletMapper.toWalletOutputDtoWithWalletType(updatedWallet, walletTypeOutputDto);
    }

    @Override
    public Boolean deleteById(UUID walletId, UUID profileId) {
        log.info("IN deleteById, walletId = {}, profileId = {}", walletId, profileId);
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        walletRepository.findById(walletId, dataSource).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return walletRepository.deleteById(walletId, dataSource);
    }
}
