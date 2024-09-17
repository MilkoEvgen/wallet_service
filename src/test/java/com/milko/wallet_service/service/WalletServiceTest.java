package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.WalletMapper;
import com.milko.wallet_service.model.Wallet;
import com.milko.wallet_service.repository.WalletRepository;
import com.milko.wallet_service.service.impl.WalletServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import com.milko.wallet_service.transaction.TransactionManager;
import com.milko.wallet_service.transaction.TransactionalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private ShardService shardService;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletStatusHistoryService walletStatusHistoryService;
    @Mock
    private WalletTypeService walletTypeService;
    @Mock
    private WalletMapper walletMapper;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private WalletServiceImpl walletService;

    private WalletInputDto walletInputDto;
    private WalletOutputDto walletOutputDto;
    private WalletTypeOutputDto walletTypeOutputDto;
    private ChangeWalletInputDto changeWalletInputDto;
    private Wallet wallet;
    private UUID walletId;
    private UUID profileUid;
    private UUID walletTypeId;

    @BeforeEach
    public void init() {
        walletId = UUID.randomUUID();
        profileUid = UUID.randomUUID();
        walletTypeId = UUID.randomUUID();

        walletInputDto = WalletInputDto.builder()
                .uuid(walletId)
                .name("Test Wallet")
                .walletTypeId(walletTypeId)
                .profileUid(profileUid)
                .balance(new BigDecimal("100.00"))
                .status(Status.ACTIVE)
                .build();

        wallet = Wallet.builder()
                .uuid(walletId)
                .createdAt(LocalDateTime.now())
                .balance(new BigDecimal("100.00"))
                .profileUid(profileUid)
                .status(Status.ACTIVE)
                .build();

        walletOutputDto = WalletOutputDto.builder()
                .uuid(walletId)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .name("Test Wallet")
                .profileUid(profileUid)
                .balance(new BigDecimal("100.00"))
                .status(Status.ACTIVE)
                .walletType(WalletTypeOutputDto.builder().uuid(walletTypeId).name("Test Type").build())
                .build();

        walletTypeOutputDto = WalletTypeOutputDto.builder().build();

        changeWalletInputDto = ChangeWalletInputDto.builder()
                .walletId(UUID.randomUUID())
                .changedByUserUid(UUID.randomUUID())
                .changedByProfileType("USER")
                .reason("Account upgrade")
                .comment("Upgrade to premium account")
                .toStatus(Status.ACTIVE)
                .build();
    }

    @Test
    public void createWalletTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletTypeService.findById(any())).thenReturn(walletOutputDto.getWalletType());
        Mockito.when(walletRepository.create(any(), any())).thenReturn(wallet);
        Mockito.when(walletMapper.toWalletOutputDtoWithWalletType(any(), any())).thenReturn(walletOutputDto);

        WalletOutputDto result = walletService.create(walletInputDto);

        assertThat(result).usingRecursiveComparison().isEqualTo(walletOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletTypeService).findById(any());
        Mockito.verify(walletRepository).create(any(), any());
        Mockito.verify(walletMapper).toWalletOutputDtoWithWalletType(any(), any());
    }

    @Test
    public void createWalletShouldThrowExceptionWhenWalletTypeNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletTypeService.findById(any())).thenThrow(new NotFoundException("Wallet Type not found", LocalDateTime.now()));

        assertThatThrownBy(() -> walletService.create(walletInputDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Wallet Type not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletTypeService).findById(any());
    }

    @Test
    public void topUpTest() {
        BigDecimal amount = new BigDecimal("50.00");
        BigDecimal newBalance = wallet.getBalance().add(amount);

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenReturn(wallet);
        Mockito.when(walletRepository.updateBalance(any(), any(), any())).thenReturn(true);

        Boolean result = walletService.topUp(walletId, amount, profileUid);

        assertThat(result).isTrue();

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(any(), any());
        Mockito.verify(walletRepository).updateBalance(walletId, newBalance, dataSource);
    }

    @Test
    public void topUpShouldThrowExceptionWhenWalletNotFound() {
        BigDecimal amount = new BigDecimal("50.00");

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenThrow(new NotFoundException("Wallet not found", LocalDateTime.now()));

        assertThatThrownBy(() -> walletService.topUp(walletId, amount, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Wallet not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(walletId, dataSource);
    }

    @Test
    public void withdrawTest() {
        BigDecimal amount = new BigDecimal("50.00");
        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenReturn(wallet);
        Mockito.when(walletRepository.updateBalance(any(), any(), any())).thenReturn(true);

        Boolean result = walletService.withdraw(walletId, amount, profileUid);

        assertThat(result).isTrue();

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(any(), any());
        Mockito.verify(walletRepository).updateBalance(walletId, newBalance, dataSource);
    }

    @Test
    public void withdrawShouldThrowLowBalanceException() {
        BigDecimal amount = new BigDecimal("150.00");

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenReturn(wallet);

        assertThatThrownBy(() -> walletService.withdraw(walletId, amount, profileUid))
                .isInstanceOf(LowBalanceException.class)
                .hasMessageContaining("Current balance " + wallet.getBalance() + " is less than required " + amount);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(walletId, dataSource);
    }

    @Test
    public void findByIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenReturn(wallet);
        Mockito.when(walletTypeService.findById(any())).thenReturn(walletOutputDto.getWalletType());
        Mockito.when(walletMapper.toWalletOutputDtoWithWalletType(any(), any())).thenReturn(walletOutputDto);

        WalletOutputDto result = walletService.findById(walletId, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(walletOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(walletId, dataSource);
        Mockito.verify(walletTypeService).findById(wallet.getWalletTypeId());
        Mockito.verify(walletMapper).toWalletOutputDtoWithWalletType(any(), any());
    }

    @Test
    public void findByIdShouldThrowNotFoundExceptionWhenWalletNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenThrow(new NotFoundException("Wallet not found", LocalDateTime.now()));

        assertThatThrownBy(() -> walletService.findById(walletId, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Wallet not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(walletId, dataSource);
    }

    @Test
    public void findAllByProfileIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findAllByUserId(any(), any())).thenReturn(List.of(wallet));
        Mockito.when(walletMapper.toWalletOutputDtoWithWalletType(any(), any())).thenReturn(walletOutputDto);

        List<WalletOutputDto> result = walletService.findAllByProfileId(profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(List.of(walletOutputDto));

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findAllByUserId(profileUid, dataSource);
        Mockito.verify(walletMapper).toWalletOutputDtoWithWalletType(any(), any());
    }

    @Test
    public void findAllByProfileIdShouldReturnEmptyListWhenNoWalletsFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findAllByUserId(any(), any())).thenReturn(List.of());

        List<WalletOutputDto> result = walletService.findAllByProfileId(profileUid);

        assertThat(result).isEmpty();

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findAllByUserId(profileUid, dataSource);
    }

    @Test
    public void updateStatusTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenReturn(wallet);
        Mockito.when(walletRepository.updateStatus(any(), any(), any())).thenReturn(true);
        Mockito.when(walletTypeService.findById(any())).thenReturn(walletTypeOutputDto);
        Mockito.when(walletMapper.toWalletOutputDtoWithWalletType(any(), any())).thenReturn(walletOutputDto);

        Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                .thenAnswer(invocation -> {
                    TransactionalTask<Boolean> task = invocation.getArgument(1);
                    return task.execute(TransactionContext.get());
                });

        WalletOutputDto result = walletService.updateStatus(changeWalletInputDto);

        assertThat(result).usingRecursiveComparison().isEqualTo(walletOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository, Mockito.times(2)).findById(any(), any());
        Mockito.verify(walletRepository).updateStatus(any(), any(), any());
        Mockito.verify(walletTypeService).findById(any());
        Mockito.verify(walletMapper).toWalletOutputDtoWithWalletType(any(), any());
    }

    @Test
    public void updateStatusShouldThrowNotFoundExceptionWhenWalletNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenThrow(new NotFoundException("Wallet not found", LocalDateTime.now()));

        Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                .thenAnswer(invocation -> {
                    TransactionalTask<Boolean> task = invocation.getArgument(1);
                    return task.execute(TransactionContext.get());
                });

        assertThatThrownBy(() -> walletService.updateStatus(changeWalletInputDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Wallet not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(changeWalletInputDto.getWalletId(), dataSource);
    }

    @Test
    public void deleteByIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenReturn(wallet);
        Mockito.when(walletRepository.deleteById(any(), any())).thenReturn(true);

        Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                .thenAnswer(invocation -> {
                    TransactionalTask<Boolean> task = invocation.getArgument(1);
                    return task.execute(TransactionContext.get());
                });

        Boolean result = walletService.deleteById(walletId, profileUid);

        assertThat(result).isTrue();
        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(any(), any());
        Mockito.verify(walletRepository).deleteById(walletId, dataSource);
    }

    @Test
    public void deleteByIdShouldThrowNotFoundExceptionWhenWalletNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(walletRepository.findById(any(), any())).thenThrow(new NotFoundException("Wallet not found", LocalDateTime.now()));

        Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                .thenAnswer(invocation -> {
                    TransactionalTask<Boolean> task = invocation.getArgument(1);
                    return task.execute(TransactionContext.get());
                });

        assertThatThrownBy(() -> walletService.deleteById(walletId, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Wallet not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(walletRepository).findById(walletId, dataSource);
    }


}
