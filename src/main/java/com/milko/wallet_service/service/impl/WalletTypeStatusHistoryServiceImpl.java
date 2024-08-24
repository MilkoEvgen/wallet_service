package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.model.WalletTypeStatusHistory;
import com.milko.wallet_service.repository.WalletTypeStatusHistoryRepository;
import com.milko.wallet_service.service.WalletTypeStatusHistoryService;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTypeStatusHistoryServiceImpl implements WalletTypeStatusHistoryService {
    private final WalletTypeStatusHistoryRepository walletTypeStatusHistoryRepository;
    private final ShardService shardService;

    @Override
    public void create(ChangeWalletTypeInputDto changeWalletTypeInputDto, Long generatedId, Status fromStatus, DataSource dataSource) {
        WalletTypeStatusHistory walletTypeStatusHistory = createWalletTypeStatusHistory(changeWalletTypeInputDto);
        walletTypeStatusHistory.setId(generatedId);
        walletTypeStatusHistory.setFromStatus(fromStatus);
        walletTypeStatusHistoryRepository.create(walletTypeStatusHistory, dataSource);
    }

    public void rollbackCreate(Long id, DataSource dataSource) {
        walletTypeStatusHistoryRepository.rollbackCreate(id, dataSource);
    }

    @Override
    public List<WalletTypeStatusHistory> findAllByWalletTypeId(Integer walletTypeStatusHistoryId) {
        DataSource dataSource = shardService.getRandomDataSource();
        return walletTypeStatusHistoryRepository.findAllByWalletTypeId(walletTypeStatusHistoryId, dataSource);
    }

    @Override
    public Long getMaxId(DataSource dataSource) {
        return walletTypeStatusHistoryRepository.getMaxId(shardService.getRandomDataSource());
    }

    private WalletTypeStatusHistory createWalletTypeStatusHistory(ChangeWalletTypeInputDto changeWalletTypeInputDto) {
        return WalletTypeStatusHistory.builder()
                .createdAt(LocalDateTime.now())
                .walletTypeId(changeWalletTypeInputDto.getWalletTypeId())
                .changedByUserUid(changeWalletTypeInputDto.getChangedByUserUid())
                .changedByProfileType(changeWalletTypeInputDto.getChangedByProfileType())
                .reason(changeWalletTypeInputDto.getReason())
                .comment(changeWalletTypeInputDto.getComment())
                .toStatus(changeWalletTypeInputDto.getToStatus())
                .build();
    }
}
