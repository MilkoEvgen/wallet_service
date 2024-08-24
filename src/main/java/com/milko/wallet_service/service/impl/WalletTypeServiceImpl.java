package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.exceptions.ShardServiceException;
import com.milko.wallet_service.mapper.WalletTypeMapper;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.repository.WalletTypeRepository;
import com.milko.wallet_service.service.WalletTypeService;
import com.milko.wallet_service.service.WalletTypeStatusHistoryService;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletTypeServiceImpl implements WalletTypeService {
    private final WalletTypeRepository walletTypeRepository;
    private final WalletTypeStatusHistoryService walletTypeStatusHistoryService;
    private final WalletTypeMapper walletTypeMapper;
    private final ShardService shardService;


    @Override
    public Integer create(WalletTypeInputDto walletTypeInputDto) {
        log.info("IN WalletTypeServiceImpl create(), walletTypeInputDto = {}", walletTypeInputDto);
        Integer maxId = walletTypeRepository.getMaxId(shardService.getRandomDataSource());
        Integer generatedId = (maxId != null) ? maxId + 1 : 1;
        walletTypeInputDto.setId(generatedId);

        WalletType walletType = walletTypeMapper.toWalletType(walletTypeInputDto);

        List<DataSource> dataSources = shardService.getAllDataSources();
        List<DataSource> processedDataSources = new ArrayList<>();
        try {
            for (DataSource dataSource : dataSources) {
                walletTypeRepository.create(walletType, dataSource);
                processedDataSources.add(dataSource);
            }
        } catch (Exception e){
            rollbackCreateForProcessedElements(generatedId, processedDataSources);
            throw new ShardServiceException(e.getMessage());
        }
        return generatedId;
    }

    private void rollbackCreateForProcessedElements(Integer id, List<DataSource> processedElements){
        for (DataSource dataSource : processedElements) {
            walletTypeRepository.rollbackCreate(id, dataSource);
        }
    }

    @Override
    public WalletTypeOutputDto findById(Integer id) {
        log.info("IN WalletTypeServiceImpl findById(), id = {}", id);
        return walletTypeMapper.toWalletTypeOutputDto(walletTypeRepository.findById(id, shardService.getRandomDataSource()));
    }

    @Override
    public List<WalletTypeOutputDto> findAll() {
        log.info("IN WalletTypeServiceImpl findAll()");
        return walletTypeRepository.findAll(shardService.getRandomDataSource()).stream()
                .map(walletTypeMapper::toWalletTypeOutputDto)
                .toList();
    }

    @Override
    public WalletTypeOutputDto update(ChangeWalletTypeInputDto changeWalletTypeInputDto) {
        log.info("IN WalletTypeServiceImpl update(), changeWalletTypeInputDto = {}", changeWalletTypeInputDto);
        Integer walletTypeId = changeWalletTypeInputDto.getWalletTypeId();

        WalletType oldWalletType = walletTypeRepository.findById(changeWalletTypeInputDto.getWalletTypeId(), shardService.getRandomDataSource());
        Status oldStatus = oldWalletType.getStatus();
        Status newStatus = changeWalletTypeInputDto.getToStatus();
        List<DataSource> dataSources = shardService.getAllDataSources();
        List<DataSource> processedDataSources = new ArrayList<>();

        Long maxId = walletTypeStatusHistoryService.getMaxId(shardService.getRandomDataSource());
        Long generatedId = (maxId != null) ? maxId + 1 : 1;
        try {
            for (DataSource dataSource : dataSources) {
                walletTypeRepository.updateStatus(newStatus, walletTypeId, changeWalletTypeInputDto.getChangedByUserUid(), dataSource);
                walletTypeStatusHistoryService.create(changeWalletTypeInputDto, generatedId, oldStatus, dataSource);
                processedDataSources.add(dataSource);
            }
        } catch (Exception e){
            rollbackUpdateForProcessedElements(oldWalletType, generatedId, processedDataSources);
            throw new ShardServiceException(e.getMessage());
        }
        return walletTypeMapper.toWalletTypeOutputDto(walletTypeRepository.findById(walletTypeId, shardService.getRandomDataSource()));
    }

    private void rollbackUpdateForProcessedElements(WalletType oldWalletType, Long walletTypeStatusHistoryId, List<DataSource> processedDataSources){
        for (DataSource dataSource : processedDataSources) {
            walletTypeRepository.rollbackUpdate(oldWalletType.getStatus(), oldWalletType.getId(), oldWalletType.getModifiedAt(), oldWalletType.getModifier(), dataSource);
            walletTypeStatusHistoryService.rollbackCreate(walletTypeStatusHistoryId, dataSource);
        }
    }


    @Override
    public Boolean deleteById(Integer id) {
        log.info("IN WalletTypeServiceImpl deleteById(), id = {}", id);
        List<DataSource> dataSources = shardService.getAllDataSources();
        List<DataSource> processedDataSources = new ArrayList<>();
        try {
            for (DataSource dataSource : dataSources) {
                walletTypeRepository.deleteById(id, dataSource);
                processedDataSources.add(dataSource);
            }
        } catch (Exception e){
            rollbackDeleteForProcessedElements(id, processedDataSources);
            throw new ShardServiceException(e.getMessage());
        }
        return true;
    }

    @Override
    public String getCurrentStatusByWalletId(Integer id) {
        DataSource dataSource = shardService.getRandomDataSource();
        return walletTypeRepository.getCurrentStatusByWalletId(id, dataSource);
    }

    private void rollbackDeleteForProcessedElements(Integer id, List<DataSource> processedDataSources){
        for (DataSource dataSource : processedDataSources) {
            walletTypeRepository.rollbackDeleteById(id, dataSource);
        }
    }
}
