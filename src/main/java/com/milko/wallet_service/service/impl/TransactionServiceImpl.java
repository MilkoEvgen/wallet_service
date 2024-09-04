package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.TransactionMapper;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.repository.TransactionRepository;
import com.milko.wallet_service.service.TransactionService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final ShardService shardService;
    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public TransactionOutputDto create(Transaction transaction) {
        log.info("IN create, transaction = {}", transaction);
        DataSource dataSource = getDataSource(transaction.getProfileUid());
        return mapper.toTransactionOutputDto(repository.create(transaction, dataSource));
    }

    @Override
    public TransactionOutputDto findById(UUID transactionId, UUID profileUid) {
        log.info("IN findById, transactionId = {}", transactionId);
        DataSource dataSource = getDataSource(profileUid);
        Transaction transaction = repository.findById(transactionId, dataSource)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        return mapper.toTransactionOutputDto(transaction);
    }

    @Override
    public TransactionOutputDto complete(UUID transactionId, UUID profileUid) {
        log.info("IN complete, transactionId = {}", transactionId);
        DataSource dataSource = getDataSource(profileUid);
        Transaction transaction = repository.updateStatus(transactionId, TransactionStatus.COMPLETED, dataSource);
        return mapper.toTransactionOutputDto(transaction);
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
