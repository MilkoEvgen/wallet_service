package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.output.TopUpRequestOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.TopUpRequestMapper;
import com.milko.wallet_service.model.TopUpRequest;
import com.milko.wallet_service.repository.TopUpRequestRepository;
import com.milko.wallet_service.service.TopUpRequestService;
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
public class TopUpRequestServiceImpl implements TopUpRequestService {
    private final ShardService shardService;
    private final TopUpRequestRepository repository;
    private final TopUpRequestMapper mapper;

    @Override
    public TopUpRequestOutputDto create(UUID paymentRequestUid, UUID profileUid) {
        log.info("IN create, paymentRequestUid = {}", paymentRequestUid);
        DataSource dataSource = getDataSource(profileUid);
        TopUpRequest request = TopUpRequest.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestUid)
                .build();
        return mapper.toTopUpRequestOutputDto(repository.create(request, dataSource));
    }

    @Override
    public TopUpRequestOutputDto findById(UUID requestId, UUID profileUid) {
        log.info("IN findById, requestId = {}", requestId);
        DataSource dataSource = getDataSource(profileUid);
        TopUpRequest request = repository.findById(requestId, dataSource);
        return mapper.toTopUpRequestOutputDto(request);
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
