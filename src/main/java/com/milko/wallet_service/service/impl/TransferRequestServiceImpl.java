package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.model.TransferRequest;
import com.milko.wallet_service.repository.TransferRequestRepository;
import com.milko.wallet_service.service.TransferRequestService;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferRequestServiceImpl implements TransferRequestService {
    private final TransferRequestRepository requestRepository;
    private final ShardService shardService;

    @Override
    public TransferRequest create(TransferRequest request, UUID profileUid) {
        log.info("in create, TransferRequest = {}", request);
        DataSource dataSource = shardService.getDataSourceByUuid(profileUid);
        return requestRepository.create(request, dataSource);
    }

    @Override
    public TransferRequest findById(UUID uuid, UUID profileUid) {
        log.info("in findById, UUID = {}", uuid);
        DataSource dataSource = shardService.getDataSourceByUuid(profileUid);
        return requestRepository.findById(uuid, dataSource)
                .orElseThrow(() -> new NotFoundException("TransferRequest not found"));
    }

    @Override
    public TransferRequest findByPaymentRequestId(UUID paymentRequestId, UUID profileUid) {
        log.info("in findByPaymentRequestId, PaymentRequestId = {}", paymentRequestId);
        DataSource dataSource = shardService.getDataSourceByUuid(profileUid);
        return requestRepository.findByPaymentRequestId(paymentRequestId, dataSource)
                .orElseThrow(() -> new NotFoundException("TransferRequest not found"));
    }
}
