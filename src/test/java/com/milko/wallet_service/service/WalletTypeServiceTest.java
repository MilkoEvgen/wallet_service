package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.mapper.WalletTypeMapper;
import com.milko.wallet_service.model.WalletType;
import com.milko.wallet_service.repository.WalletTypeRepository;
import com.milko.wallet_service.service.impl.WalletTypeServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import com.milko.wallet_service.transaction.TransactionManager;
import com.milko.wallet_service.transaction.TransactionalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
public class WalletTypeServiceTest {

    @Mock
    private WalletTypeRepository walletTypeRepository;
    @Mock
    private WalletTypeStatusHistoryService walletTypeStatusHistoryService;
    @Mock
    private WalletTypeMapper walletTypeMapper;
    @Mock
    private ShardService shardService;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private TransactionContext context;
    @Mock
    private Connection connection;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private WalletTypeServiceImpl walletTypeService;

    private WalletTypeInputDto walletTypeInputDto;
    private WalletTypeOutputDto walletTypeOutputDto;
    private WalletType walletType;
    private ChangeWalletTypeInputDto changeWalletTypeInputDto;
    private UUID walletTypeId;
    private UUID profileUid;

    @BeforeEach
    public void init() {
        walletTypeId = UUID.randomUUID();
        profileUid = UUID.randomUUID();

        walletTypeInputDto = WalletTypeInputDto.builder()
                .uuid(walletTypeId)
                .name("Test Wallet Type")
                .status(Status.ACTIVE)
                .build();

        walletType = WalletType.builder()
                .uuid(walletTypeId)
                .name("Test Wallet Type")
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        walletTypeOutputDto = WalletTypeOutputDto.builder()
                .uuid(walletTypeId)
                .name("Test Wallet Type")
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        changeWalletTypeInputDto = ChangeWalletTypeInputDto.builder()
                .walletTypeId(walletTypeId)
                .changedByUserUid(profileUid)
                .toStatus(Status.ACTIVE)
                .build();
    }

    @Test
    public void createWalletTypeTest() {
        try (MockedStatic<TransactionContext> mockedStatic = Mockito.mockStatic(TransactionContext.class)) {
            mockedStatic.when(TransactionContext::get).thenReturn(context);
            Mockito.when(context.getConnection(any())).thenReturn(connection);
            Mockito.when(walletTypeMapper.toWalletType(any())).thenReturn(walletType);
            Mockito.when(shardService.getAllDataSources()).thenReturn(List.of(dataSource));

            Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                    .thenAnswer(invocation -> {
                        TransactionalTask<UUID> task = invocation.getArgument(1);
                        return task.execute(TransactionContext.get());
                    });

            UUID result = walletTypeService.create(walletTypeInputDto);

            assertThat(result).isEqualTo(walletTypeInputDto.getUuid());

            Mockito.verify(walletTypeMapper).toWalletType(any());
            Mockito.verify(shardService).getAllDataSources();
            Mockito.verify(walletTypeRepository).create(any(), any());
            Mockito.verify(context).getConnection(any());
            Mockito.verify(transactionManager).executeInTransaction(anyList(), any());
        }
    }

    @Test
    public void createWalletTypeShouldThrowException() {
        try (MockedStatic<TransactionContext> mockedStatic = Mockito.mockStatic(TransactionContext.class)) {
            mockedStatic.when(TransactionContext::get).thenReturn(context);
            Mockito.when(context.getConnection(any())).thenReturn(connection);
            Mockito.when(walletTypeMapper.toWalletType(any())).thenReturn(walletType);
            Mockito.when(shardService.getAllDataSources()).thenReturn(List.of(dataSource));
            Mockito.when(walletTypeRepository.create(any(), any()))
                    .thenThrow(new RuntimeException("DataSource failure"));

            Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                    .thenAnswer(invocation -> {
                        TransactionalTask<UUID> task = invocation.getArgument(1);
                        return task.execute(TransactionContext.get());
                    });

            assertThatThrownBy(() -> walletTypeService.create(walletTypeInputDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DataSource failure");

            Mockito.verify(walletTypeMapper).toWalletType(any());
            Mockito.verify(shardService).getAllDataSources();
            Mockito.verify(walletTypeRepository).create(any(), any());
            Mockito.verify(context).getConnection(any());
            Mockito.verify(transactionManager).executeInTransaction(anyList(), any());
        }
    }

    @Test
    public void findByIdTest() {
        Mockito.when(shardService.getRandomDataSource()).thenReturn(dataSource);
        Mockito.when(walletTypeRepository.findById(any(), any())).thenReturn(walletType);
        Mockito.when(walletTypeMapper.toWalletTypeOutputDto(any())).thenReturn(walletTypeOutputDto);

        WalletTypeOutputDto result = walletTypeService.findById(walletTypeId);

        assertThat(result).usingRecursiveComparison().isEqualTo(walletTypeOutputDto);

        Mockito.verify(shardService).getRandomDataSource();
        Mockito.verify(walletTypeRepository).findById(walletTypeId, dataSource);
        Mockito.verify(walletTypeMapper).toWalletTypeOutputDto(walletType);
    }

    @Test
    public void findByIdShouldThrowException() {
        Mockito.when(shardService.getRandomDataSource()).thenReturn(dataSource);
        Mockito.when(walletTypeRepository.findById(any(), any())).thenThrow(new RuntimeException("Wallet Type not found"));

        assertThatThrownBy(() -> walletTypeService.findById(walletTypeId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wallet Type not found");

        Mockito.verify(shardService).getRandomDataSource();
        Mockito.verify(walletTypeRepository).findById(walletTypeId, dataSource);
    }

    @Test
    public void updateWalletTypeTest() {
        try (MockedStatic<TransactionContext> mockedStatic = Mockito.mockStatic(TransactionContext.class)) {
            mockedStatic.when(TransactionContext::get).thenReturn(context);
            Mockito.when(context.getConnection(any())).thenReturn(connection);
            Mockito.when(shardService.getAllDataSources()).thenReturn(List.of(dataSource));
            Mockito.when(walletTypeRepository.findById(any(), any())).thenReturn(walletType);
            Mockito.when(walletTypeMapper.toWalletTypeOutputDto(any())).thenReturn(walletTypeOutputDto);


            Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                    .thenAnswer(invocation -> {
                        TransactionalTask<UUID> task = invocation.getArgument(1);
                        return task.execute(TransactionContext.get());
                    });

            WalletTypeOutputDto result = walletTypeService.update(changeWalletTypeInputDto);

            assertThat(result).usingRecursiveComparison().isEqualTo(walletTypeOutputDto);

            Mockito.verify(context, Mockito.times(2)).getConnection(any());
            Mockito.verify(transactionManager).executeInTransaction(anyList(), any());
            Mockito.verify(shardService).getAllDataSources();
            Mockito.verify(walletTypeRepository).findById(any(), any());
            Mockito.verify(walletTypeMapper).toWalletTypeOutputDto(any());
        }
    }

    @Test
    public void updateWalletTypeShouldThrowException() {
        try (MockedStatic<TransactionContext> mockedStatic = Mockito.mockStatic(TransactionContext.class)) {
            mockedStatic.when(TransactionContext::get).thenReturn(context);
            Mockito.when(context.getConnection(any())).thenReturn(connection);
            Mockito.when(shardService.getAllDataSources()).thenReturn(List.of(dataSource));
            Mockito.when(walletTypeRepository.findById(any(), any())).thenReturn(walletType);

            Mockito.when(walletTypeRepository.updateStatus(any(), any(), any()))
                    .thenThrow(new RuntimeException("Status not updated"));


            Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                    .thenAnswer(invocation -> {
                        TransactionalTask<UUID> task = invocation.getArgument(1);
                        return task.execute(TransactionContext.get());
                    });

            assertThatThrownBy(() -> walletTypeService.update(changeWalletTypeInputDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Status not updated");

            Mockito.verify(context).getConnection(any());
            Mockito.verify(transactionManager).executeInTransaction(anyList(), any());
            Mockito.verify(shardService).getAllDataSources();
            Mockito.verify(walletTypeRepository).findById(any(), any());
            Mockito.verify(walletTypeRepository).updateStatus(any(), any(), any());
        }
    }

    @Test
    public void deleteByIdTest() {
        try (MockedStatic<TransactionContext> mockedStatic = Mockito.mockStatic(TransactionContext.class)) {
            mockedStatic.when(TransactionContext::get).thenReturn(context);
            Mockito.when(context.getConnection(any())).thenReturn(connection);
            Mockito.when(shardService.getAllDataSources()).thenReturn(List.of(dataSource));

            Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                    .thenAnswer(invocation -> {
                        TransactionalTask<UUID> task = invocation.getArgument(1);
                        return task.execute(TransactionContext.get());
                    });

            Boolean result = walletTypeService.deleteById(UUID.randomUUID());

            assertThat(result).isTrue();

            Mockito.verify(context).getConnection(any());
            Mockito.verify(transactionManager).executeInTransaction(anyList(), any());
            Mockito.verify(shardService).getAllDataSources();
            Mockito.verify(walletTypeRepository).deleteById(any(), any());
        }
    }

    @Test
    public void deleteByIdShouldThrowExceptionWhenWalletTypeNotFound() {
        try (MockedStatic<TransactionContext> mockedStatic = Mockito.mockStatic(TransactionContext.class)) {
            mockedStatic.when(TransactionContext::get).thenReturn(context);
            Mockito.when(context.getConnection(any())).thenReturn(connection);
            Mockito.when(shardService.getAllDataSources()).thenReturn(List.of(dataSource));
            Mockito.when(walletTypeRepository.deleteById(any(), any())).thenThrow(new RuntimeException("Wallet Type not found"));

            Mockito.when(transactionManager.executeInTransaction(anyList(), any()))
                    .thenAnswer(invocation -> {
                        TransactionalTask<UUID> task = invocation.getArgument(1);
                        return task.execute(TransactionContext.get());
                    });

            assertThatThrownBy(() -> walletTypeService.deleteById(walletTypeId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Wallet Type not found");

            Mockito.verify(context).getConnection(any());
            Mockito.verify(transactionManager).executeInTransaction(anyList(), any());
            Mockito.verify(shardService).getAllDataSources();
            Mockito.verify(walletTypeRepository).deleteById(any(), any());
        }
    }
}
