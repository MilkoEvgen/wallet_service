package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.mapper.WalletTypeMapper;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.repository.WalletTypeRepository;
import com.milko.wallet_service.service.WalletTypeService;
import com.milko.wallet_service.service.WalletTypeStatusHistoryService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import com.milko.wallet_service.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletTypeServiceImpl implements WalletTypeService {
    private final WalletTypeRepository walletTypeRepository;
    private final WalletTypeStatusHistoryService walletTypeStatusHistoryService;
    private final WalletTypeMapper walletTypeMapper;
    private final ShardService shardService;
    private final TransactionManager transactionManager;


    @Override
    public UUID create(WalletTypeInputDto walletTypeInputDto) {
        log.info("IN WalletTypeServiceImpl create(), walletTypeInputDto = {}", walletTypeInputDto);
        UUID generatedId = UUID.randomUUID();
        walletTypeInputDto.setUuid(generatedId);
        WalletType walletType = walletTypeMapper.toWalletType(walletTypeInputDto);
        List<DataSource> dataSources = shardService.getAllDataSources();

        return transactionManager.executeInTransaction(dataSources, context -> {
            for (DataSource dataSource : dataSources) {
                 walletTypeRepository.create(walletType, getTransactionalDataSource(dataSource));
            }
            return generatedId;
        });
    }

    @Override
    public WalletTypeOutputDto findById(UUID uuid) {
        log.info("IN WalletTypeServiceImpl findById(), uuid = {}", uuid);
        return walletTypeMapper.toWalletTypeOutputDto(walletTypeRepository.findById(uuid, shardService.getRandomDataSource()));
    }

    @Override
    public List<WalletTypeOutputDto> findAll() {
        log.info("IN WalletTypeServiceImpl findAll()");
        return walletTypeRepository.findAll(shardService.getRandomDataSource()).stream()
                .map(walletTypeMapper::toWalletTypeOutputDto)
                .toList();
    }

    @Override
    public WalletTypeOutputDto update(ChangeWalletTypeInputDto dto) {
        log.info("IN WalletTypeServiceImpl update(), dto = {}", dto);
        List<DataSource> dataSources = shardService.getAllDataSources();
        WalletType walletType = walletTypeRepository.findById(dto.getWalletTypeId(), shardService.getRandomDataSource());
        Status newStatus = dto.getToStatus();
        LocalDateTime modifyingTime = LocalDateTime.now();
        UUID generatedId = UUID.randomUUID();

        return transactionManager.executeInTransaction(dataSources, context -> {
            for (DataSource dataSource : dataSources) {
                walletTypeRepository.updateStatus(dto, modifyingTime, getTransactionalDataSource(dataSource));
                walletTypeStatusHistoryService.create(dto, generatedId, walletType.getStatus(), getTransactionalDataSource(dataSource));
            }
            walletType.setStatus(newStatus);
            walletType.setModifiedAt(modifyingTime);
            walletType.setModifier(dto.getChangedByUserUid().toString());
            return walletTypeMapper.toWalletTypeOutputDto(walletType);
        });
    }


    @Override
    public Boolean deleteById(UUID uuid) {
        log.info("IN WalletTypeServiceImpl deleteById(), id = {}", uuid);
        List<DataSource> dataSources = shardService.getAllDataSources();

        return transactionManager.executeInTransaction(dataSources, context -> {
            for (DataSource dataSource : dataSources) {
                walletTypeRepository.deleteById(uuid, getTransactionalDataSource(dataSource));
            }
            return true;
        });
    }

    private DataSource getTransactionalDataSource(DataSource dataSource){
        TransactionContext context = TransactionContext.get();
        Connection connection = context.getConnection(dataSource);
        return new SingleConnectionDataSource(connection, false);
    }
}
