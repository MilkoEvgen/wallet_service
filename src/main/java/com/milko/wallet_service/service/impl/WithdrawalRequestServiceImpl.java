package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.output.WithdrawalRequestOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.WithdrawalRequestMapper;
import com.milko.wallet_service.model.WithdrawalRequest;
import com.milko.wallet_service.repository.WithdrawalRequestRepository;
import com.milko.wallet_service.service.WithdrawalRequestService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalRequestServiceImpl implements WithdrawalRequestService {
    private final ShardService shardService;
    private final WithdrawalRequestRepository repository;
    private final WithdrawalRequestMapper mapper;

    @Override
    public WithdrawalRequestOutputDto create(UUID paymentRequestUid, UUID profileUid) {
        log.info("IN create, paymentRequestUid = {}", paymentRequestUid);
        DataSource dataSource = getDataSource(profileUid);
        WithdrawalRequest request = WithdrawalRequest.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestUid)
                .build();
        return mapper.toWithdrawalRequestOutputDto(repository.create(request, dataSource));
    }

    @Override
    public WithdrawalRequestOutputDto findById(UUID requestId, UUID profileUid) {
        log.info("IN findById, requestId = {}", requestId);
        DataSource dataSource = getDataSource(profileUid);
        WithdrawalRequest request = repository.findById(requestId, dataSource);
        return mapper.toWithdrawalRequestOutputDto(request);
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
