package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.output.WalletStatusHistoryOutputDto;
import com.milko.wallet_service.mapper.WalletStatusHistoryMapper;
import com.milko.wallet_service.model.WalletStatusHistory;
import com.milko.wallet_service.repository.WalletStatusHistoryRepository;
import com.milko.wallet_service.service.impl.WalletStatusHistoryServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class WalletStatusHistoryServiceTest {

    @Mock
    private ShardService shardService;
    @Mock
    private WalletStatusHistoryRepository walletStatusHistoryRepository;
    @Mock
    private WalletStatusHistoryMapper walletStatusHistoryMapper;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private WalletStatusHistoryServiceImpl walletStatusHistoryService;

    private ChangeWalletInputDto changeWalletInputDto;
    private WalletStatusHistory walletStatusHistory;
    private WalletStatusHistoryOutputDto walletStatusHistoryOutputDto;
    private UUID walletId;
    private UUID profileUid;
    private Status fromStatus;

    @BeforeEach
    public void init() {
        walletId = UUID.randomUUID();
        profileUid = UUID.randomUUID();
        fromStatus = Status.ACTIVE;

        changeWalletInputDto = ChangeWalletInputDto.builder()
                .walletId(walletId)
                .changedByUserUid(UUID.randomUUID())
                .changedByProfileType("USER")
                .reason("Account upgrade")
                .comment("Upgrade to premium account")
                .toStatus(Status.ACTIVE)
                .build();

        walletStatusHistory = WalletStatusHistory.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .walletUid(walletId)
                .changedByUserUid(changeWalletInputDto.getChangedByUserUid())
                .changedByProfileType(changeWalletInputDto.getChangedByProfileType())
                .reason(changeWalletInputDto.getReason())
                .fromStatus(fromStatus)
                .comment(changeWalletInputDto.getComment())
                .toStatus(changeWalletInputDto.getToStatus())
                .build();

        walletStatusHistoryOutputDto = WalletStatusHistoryOutputDto.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .walletUid(walletId)
                .fromStatus(fromStatus)
                .toStatus(Status.ACTIVE)
                .comment(changeWalletInputDto.getComment())
                .build();
    }

    @Test
    public void createWalletStatusHistoryTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);

        walletStatusHistoryService.create(changeWalletInputDto, fromStatus, profileUid);

        Mockito.verify(walletStatusHistoryRepository).create(any(), any());
    }

    @Test
    public void findAllByWalletIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletStatusHistoryRepository.findAllByWalletId(any(), any())).thenReturn(List.of(walletStatusHistory));
        Mockito.when(walletStatusHistoryMapper.toWalletStatusHistoryOutputDtoList(any())).thenReturn(List.of(walletStatusHistoryOutputDto));

        List<WalletStatusHistoryOutputDto> result = walletStatusHistoryService.findAllByWalletId(walletId, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(List.of(walletStatusHistoryOutputDto));

        Mockito.verify(walletStatusHistoryRepository).findAllByWalletId(walletId, dataSource);
        Mockito.verify(walletStatusHistoryMapper).toWalletStatusHistoryOutputDtoList(any());
    }

    @Test
    public void findAllByWalletIdShouldReturnEmptyList() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletStatusHistoryRepository.findAllByWalletId(any(), any())).thenReturn(List.of());
        Mockito.when(walletStatusHistoryMapper.toWalletStatusHistoryOutputDtoList(any())).thenReturn(List.of());

        List<WalletStatusHistoryOutputDto> result = walletStatusHistoryService.findAllByWalletId(walletId, profileUid);

        assertThat(result).isEmpty();

        Mockito.verify(walletStatusHistoryRepository).findAllByWalletId(walletId, dataSource);
        Mockito.verify(walletStatusHistoryMapper).toWalletStatusHistoryOutputDtoList(List.of());
    }
}
