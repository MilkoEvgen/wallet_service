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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTypeStatusHistoryServiceImpl implements WalletTypeStatusHistoryService {
    private final WalletTypeStatusHistoryRepository walletTypeStatusHistoryRepository;
    private final ShardService shardService;

    @Override
    public void create(ChangeWalletTypeInputDto changeWalletTypeInputDto, UUID generatedId, Status fromStatus, DataSource dataSource) {
        WalletTypeStatusHistory walletTypeStatusHistory = createWalletTypeStatusHistory(changeWalletTypeInputDto);
        walletTypeStatusHistory.setUuid(generatedId);
        walletTypeStatusHistory.setFromStatus(fromStatus);
        walletTypeStatusHistoryRepository.create(walletTypeStatusHistory, dataSource);
    }

    @Override
    public List<WalletTypeStatusHistory> findAllByWalletTypeId(UUID walletTypeStatusHistoryId) {
        DataSource dataSource = shardService.getRandomDataSource();
        return walletTypeStatusHistoryRepository.findAllByWalletTypeId(walletTypeStatusHistoryId, dataSource);
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
