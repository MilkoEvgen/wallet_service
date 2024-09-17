package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.WalletMapper;
import com.milko.wallet_service.model.Wallet;
import com.milko.wallet_service.repository.WalletRepository;
import com.milko.wallet_service.service.WalletService;
import com.milko.wallet_service.service.WalletStatusHistoryService;
import com.milko.wallet_service.service.WalletTypeService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import com.milko.wallet_service.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
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
    private final TransactionManager transactionManager;

    @Override
    public WalletOutputDto create(WalletInputDto walletInputDto) {
        log.info("IN create, wallet = {}", walletInputDto);
        DataSource dataSource = getDataSource(walletInputDto.getProfileUid());
        walletInputDto.setUuid(UUID.randomUUID());
        WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(walletInputDto.getWalletTypeId());
        Wallet wallet = walletRepository.create(walletMapper.toWallet(walletInputDto), dataSource);
        return walletMapper.toWalletOutputDtoWithWalletType(wallet, walletTypeOutputDto);
    }

    @Override
    public Boolean topUp(UUID walletUid, BigDecimal amount, UUID profileId) {
        DataSource dataSource = getDataSource(profileId);
        Wallet wallet = walletRepository.findById(walletUid, dataSource);
        BigDecimal newBalance = wallet.getBalance().add(amount);
        return walletRepository.updateBalance(walletUid, newBalance, dataSource);
    }

    @Override
    public Boolean withdraw(UUID walletUid, BigDecimal amount, UUID profileId) {
        DataSource dataSource = getDataSource(profileId);
        Wallet wallet = walletRepository.findById(walletUid, dataSource);
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new LowBalanceException("Current balance " + wallet.getBalance() + " is less than required " + amount, LocalDateTime.now());
        }
        return walletRepository.updateBalance(walletUid, newBalance, dataSource);
    }

    @Override
    public WalletOutputDto findById(UUID walletId, UUID profileId) {
        log.info("IN findById, walletId = {}, profileId = {}", walletId, profileId);
        DataSource dataSource = getDataSource(profileId);
        Wallet wallet = walletRepository.findById(walletId, dataSource);
        WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(wallet.getWalletTypeId());
        return walletMapper.toWalletOutputDtoWithWalletType(wallet, walletTypeOutputDto);
    }

    @Override
    public List<WalletOutputDto> findAllByProfileId(UUID profileId) {
        log.info("IN findAllByProfileId, profileId = {}", profileId);
        DataSource dataSource = getDataSource(profileId);
        return walletRepository.findAllByUserId(profileId, dataSource).stream()
                .map(wallet -> {
                    WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(wallet.getWalletTypeId());
                    return walletMapper.toWalletOutputDtoWithWalletType(wallet, walletTypeOutputDto);
                }).collect(Collectors.toList());
    }

    @Override
    public WalletOutputDto updateStatus(ChangeWalletInputDto changeWalletInputDto) {
        log.info("IN updateStatus, changeWalletInputDto = {}", changeWalletInputDto);
        DataSource dataSource = getDataSource(changeWalletInputDto.getChangedByUserUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            Wallet wallet = walletRepository.findById(changeWalletInputDto.getWalletId(), dataSource);
            walletRepository.updateStatus(changeWalletInputDto.getWalletId(), changeWalletInputDto.getToStatus(), dataSource);
            walletStatusHistoryService.create(changeWalletInputDto, wallet.getStatus(), changeWalletInputDto.getChangedByUserUid());
            Wallet updatedWallet = walletRepository.findById(changeWalletInputDto.getWalletId(), dataSource);
            WalletTypeOutputDto walletTypeOutputDto = walletTypeService.findById(wallet.getWalletTypeId());
            return walletMapper.toWalletOutputDtoWithWalletType(updatedWallet, walletTypeOutputDto);
        });
    }

    @Override
    public Boolean deleteById(UUID walletId, UUID profileId) {
        log.info("IN deleteById, walletId = {}, profileId = {}", walletId, profileId);
        DataSource dataSource = getDataSource(profileId);
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            walletRepository.findById(walletId, dataSource);
            return walletRepository.deleteById(walletId, dataSource);
        });
    }

    private DataSource getDataSource(UUID profileId) {
        TransactionContext context = TransactionContext.get();
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);

        if (context.hasActiveTransaction()) {
            Connection connection = context.getConnection(dataSource);
            return new SingleConnectionDataSource(connection, false);
        } else {
            return dataSource;
        }
    }

}
