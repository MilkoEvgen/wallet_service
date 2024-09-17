package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.mapper.PaymentRequestMapper;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.repository.PaymentRequestRepository;
import com.milko.wallet_service.service.FeeService;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.TransferRequestService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestServiceImpl implements PaymentRequestService {
    private final PaymentRequestRepository paymentRequestRepository;
    private final TransferRequestService transferRequestService;
    private final ShardService shardService;
    private final PaymentRequestMapper paymentRequestMapper;
    private final FeeService feeService;


    @Override
    public PaymentRequestOutputDto create(PaymentRequestInputDto dto) {
        log.info("in create, PaymentRequest = {}", dto);
        DataSource dataSource = getDataSource(dto.getProfileUid());
        PaymentRequest paymentRequest = createPaymentRequest(dto);
        paymentRequest.setFee(feeService.getFee(paymentRequest));
        return paymentRequestMapper.toPaymentRequestOutputDto(paymentRequestRepository.create(paymentRequest, dataSource));
    }

    @Override
    public PaymentRequestOutputDto findById(UUID requestId, UUID profileId) {
        log.info("in findById, requestId = {}", requestId);
        DataSource dataSource = getDataSource(profileId);
        PaymentRequest paymentRequest = paymentRequestRepository.findById(requestId, dataSource);
        PaymentRequestOutputDto requestOutputDto = paymentRequestMapper.toPaymentRequestOutputDto(paymentRequest);
        if (Objects.equals(paymentRequest.getType(), RequestType.TRANSFER)){
            TransferRequestOutputDto transferRequest = transferRequestService.findByPaymentRequestId(requestId, profileId);
            requestOutputDto.setRecipientUid(transferRequest.getRecipientUid());
            requestOutputDto.setWalletUidTo(transferRequest.getWalletUidTo());
        }
        return requestOutputDto;
    }

    @Override
    public List<PaymentRequestOutputDto> findAllByUserId(UUID profileId) {
        log.info("in findAllByUserId, profileId = {}", profileId);
        DataSource dataSource = getDataSource(profileId);
        List<PaymentRequest> requests = paymentRequestRepository.findAllByUserId(profileId, dataSource);
        return paymentRequestMapper.toPaymentRequestOutputDtoList(requests);
    }

    protected PaymentRequest createPaymentRequest(PaymentRequestInputDto dto){
        PaymentRequest paymentRequest = paymentRequestMapper.toPaymentRequest(dto);
        paymentRequest.setId(UUID.randomUUID());
        LocalDateTime dateTime = LocalDateTime.now();
        paymentRequest.setCreatedAt(dateTime);
        paymentRequest.setExpiredAt(dateTime.plusMinutes(5L));
        paymentRequest.setStatus(Status.NEW);
        return paymentRequest;
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
