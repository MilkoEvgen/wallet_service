package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.mapper.TransferRequestMapper;
import com.milko.wallet_service.model.TransferRequest;
import com.milko.wallet_service.repository.TransferRequestRepository;
import com.milko.wallet_service.service.TransferRequestService;
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
public class TransferRequestServiceImpl implements TransferRequestService {
    private final TransferRequestRepository requestRepository;
    private final TransferRequestMapper mapper;
    private final ShardService shardService;

    @Override
    public TransferRequestOutputDto create(TransferRequest request, UUID profileUid) {
        log.info("in create, TransferRequest = {}", request);
        DataSource dataSource = getDataSource(profileUid);
        return mapper.toTransferRequestOutputDto(requestRepository.create(request, dataSource));
    }

    @Override
    public TransferRequestOutputDto findById(UUID uuid, UUID profileUid) {
        log.info("in findById, UUID = {}", uuid);
        DataSource dataSource = getDataSource(profileUid);
        TransferRequest request = requestRepository.findById(uuid, dataSource);
        return mapper.toTransferRequestOutputDto(request);
    }

    @Override
    public TransferRequestOutputDto findByPaymentRequestId(UUID paymentRequestId, UUID profileUid) {
        log.info("in findByPaymentRequestId, PaymentRequestId = {}", paymentRequestId);
        DataSource dataSource = getDataSource(profileUid);
        TransferRequest request = requestRepository.findByPaymentRequestId(paymentRequestId, dataSource);
        return mapper.toTransferRequestOutputDto(request);
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
