package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.TransactionMapper;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.repository.TransactionRepository;
import com.milko.wallet_service.service.impl.TransactionServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private ShardService shardService;
    @Mock
    private TransactionRepository repository;
    @Mock
    private TransactionMapper mapper;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionOutputDto transactionOutputDto;
    private Transaction transaction;
    private UUID transactionId;
    private UUID profileUid;

    @BeforeEach
    public void init() {
        transactionId = UUID.randomUUID();
        profileUid = UUID.randomUUID();

        transaction = Transaction.builder()
                .uuid(transactionId)
                .createdAt(LocalDateTime.now())
                .profileUid(profileUid)
                .build();

        transactionOutputDto = TransactionOutputDto.builder()
                .uuid(transactionId)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .linkedTransaction(UUID.randomUUID())
                .profileUid(profileUid)
                .walletUid(UUID.randomUUID())
                .walletName("Test Wallet")
                .balanceOperationAmount(new BigDecimal("100.00"))
                .rawAmount(new BigDecimal("95.00"))
                .fee(new BigDecimal("5.00"))
                .amountInUsd(new BigDecimal("100.00"))
                .type("TOP_UP")
                .state("PENDING")
                .paymentRequestUid(UUID.randomUUID())
                .currencyCode("USD")
                .refundFee(10L)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    public void createTransactionTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.create(any(), any())).thenReturn(transaction);
        Mockito.when(mapper.toTransactionOutputDto(any())).thenReturn(transactionOutputDto);

        TransactionOutputDto result = transactionService.create(transaction);

        assertThat(result).usingRecursiveComparison().isEqualTo(transactionOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).create(any(), any());
        Mockito.verify(mapper).toTransactionOutputDto(any());
    }

    @Test
    public void createTransactionShouldThrowExceptionWhenDataSourceFails() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenThrow(new RuntimeException("DataSource not found"));

        assertThatThrownBy(() -> transactionService.create(transaction))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DataSource not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
    }

    @Test
    public void findByIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.findById(any(), any())).thenReturn(transaction);
        Mockito.when(mapper.toTransactionOutputDto(any())).thenReturn(transactionOutputDto);

        TransactionOutputDto result = transactionService.findById(transactionId, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(transactionOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).findById(any(), any());
        Mockito.verify(mapper).toTransactionOutputDto(any());
    }

    @Test
    public void findByIdShouldThrowNotFoundException() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.findById(any(), any())).thenThrow(new NotFoundException("Transaction not found", LocalDateTime.now()));

        assertThatThrownBy(() -> transactionService.findById(transactionId, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Transaction not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).findById(any(), any());
    }

    @Test
    public void completeTransactionTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.updateStatus(any(), any(), any())).thenReturn(transaction);
        Mockito.when(mapper.toTransactionOutputDto(any())).thenReturn(transactionOutputDto);

        TransactionOutputDto result = transactionService.complete(transactionId, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(transactionOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).updateStatus(any(), any(), any());
        Mockito.verify(mapper).toTransactionOutputDto(any());
    }

    @Test
    public void completeTransactionShouldThrowExceptionWhenNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.updateStatus(any(), any(), any())).thenThrow(new NotFoundException("Transaction not found", LocalDateTime.now()));

        assertThatThrownBy(() -> transactionService.complete(transactionId, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Transaction not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).updateStatus(any(), any(), any());
    }
}
