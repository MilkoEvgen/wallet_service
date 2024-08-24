package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.output.WalletStatusHistoryOutputDto;
import com.milko.wallet_service.mapper.WalletStatusHistoryMapper;
import com.milko.wallet_service.model.WalletStatusHistory;
import com.milko.wallet_service.repository.WalletStatusHistoryRepository;
import com.milko.wallet_service.service.WalletStatusHistoryService;
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
public class WalletStatusHistoryServiceImpl implements WalletStatusHistoryService {
    private final ShardService shardService;
    private final WalletStatusHistoryRepository walletStatusHistoryRepository;
    private final WalletStatusHistoryMapper walletStatusHistoryMapper;

    @Override
    public void create(ChangeWalletInputDto changeWalletInputDto, Status fromStatus, UUID profileId) {
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        WalletStatusHistory walletStatusHistory = createWalletStatusHistory(changeWalletInputDto, fromStatus);
        walletStatusHistory.setUuid(UUID.randomUUID());
        walletStatusHistoryRepository.create(walletStatusHistory, dataSource);
    }

    @Override
    public List<WalletStatusHistoryOutputDto> findAllByWalletId(UUID walletId, UUID profileId) {
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);
        return walletStatusHistoryMapper.toWalletStatusHistoryOutputDtoList(walletStatusHistoryRepository.findAllByWalletId(walletId, dataSource));
    }

    private WalletStatusHistory createWalletStatusHistory(ChangeWalletInputDto changeWalletInputDto, Status fromStatus){
        return WalletStatusHistory.builder()
                .createdAt(LocalDateTime.now())
                .walletUid(changeWalletInputDto.getWalletId())
                .changedByUserUid(changeWalletInputDto.getChangedByUserUid())
                .changedByProfileType(changeWalletInputDto.getChangedByProfileType())
                .reason(changeWalletInputDto.getReason())
                .fromStatus(fromStatus)
                .comment(changeWalletInputDto.getComment())
                .toStatus(changeWalletInputDto.getToStatus())
                .build();
    }
}
