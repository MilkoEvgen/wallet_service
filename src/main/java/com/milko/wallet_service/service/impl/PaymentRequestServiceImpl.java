package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptions.AccessDeniedException;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.PaymentRequestMapper;
import com.milko.wallet_service.model.*;
import com.milko.wallet_service.repository.*;
import com.milko.wallet_service.service.FeeService;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.WalletService;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestServiceImpl implements PaymentRequestService {
    private final WalletService walletService;
    private final PaymentRequestRepository paymentRequestRepository;
    private final TopUpRequestRepository topUpRepository;
    private final WithdrawalRequestRepository withdrawalRepository;
    private final TransferRequestRepository transferRepository;
    private final ShardService shardService;
    private final FeeService feeService;
    private final PaymentRequestMapper paymentRequestMapper;


    @Override
    @Transactional
    public PaymentRequestOutputDto create(PaymentRequestInputDto dto) {
        log.info("in create, PaymentRequest = {}", dto);
        DataSource dataSource = shardService.getDataSourceByUuid(dto.getProfileUid());
        PaymentRequest paymentRequest = createPaymentRequest(dto);
        WalletOutputDto senderWallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
        checkWalletOwner(paymentRequest, senderWallet.getProfileUid());
        paymentRequest.setFee(feeService.getFee(paymentRequest));

        PaymentRequest savedRequest = paymentRequestRepository.create(paymentRequest, dataSource);

        if (Objects.equals(dto.getType(), RequestType.TOP_UP)){
            topUpRepository.create(createTopUpRequest(savedRequest.getId()), dataSource);
        } else if (Objects.equals(dto.getType(), RequestType.WITHDRAWAL)){
            withdrawalRepository.create(createWithdrawalRequest(savedRequest.getId()), dataSource);
        } else if (Objects.equals(dto.getType(), RequestType.TRANSFER)){
            WalletOutputDto recipientWallet = walletService.findById(dto.getWalletUidTo(), dto.getRecipientUid());
            if (!Objects.equals(senderWallet.getWalletType().getCurrencyCode(), recipientWallet.getWalletType().getCurrencyCode())){
                throw new RuntimeException("Wallet currencies do not match");
            }
            TransferRequest transferRequest = transferRepository.create(createTransferRequest(paymentRequest, dto), dataSource);
            PaymentRequestOutputDto outputDto = paymentRequestMapper.toPaymentRequestOutputDto(savedRequest);
            outputDto.setWalletUidTo(transferRequest.getWalletUidTo());
            return outputDto;
        }
        return paymentRequestMapper.toPaymentRequestOutputDto(savedRequest);
    }

    @Override
    public PaymentRequestOutputDto findById(UUID requestId, UUID profileId) {
        log.info("in findById, requestId = {}", requestId);
        PaymentRequest paymentRequest = paymentRequestRepository.findById(requestId, shardService.getDataSourceByUuid(profileId))
                .orElseThrow(() -> new NotFoundException("PaymentRequest with id " + requestId + " not found"));
        return paymentRequestMapper.toPaymentRequestOutputDto(paymentRequest);
    }

    @Override
    public List<PaymentRequestOutputDto> findAllByUserId(UUID profileId) {
        log.info("in findAllByUserId, profileId = {}", profileId);
        List<PaymentRequest> requests = paymentRequestRepository.findAllByUserId(profileId, shardService.getDataSourceByUuid(profileId));
        return paymentRequestMapper.toPaymentRequestOutputDtoList(requests);
    }

    private TopUpRequest createTopUpRequest(UUID requestId){
        return TopUpRequest.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(requestId)
                .build();
    }

    private WithdrawalRequest createWithdrawalRequest(UUID requestId){
        return WithdrawalRequest.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(requestId)
                .build();
    }

    private TransferRequest createTransferRequest(PaymentRequest paymentRequest, PaymentRequestInputDto dto){
        return TransferRequest.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequest.getId())
                .recipientUid(dto.getRecipientUid())
                .walletUidTo(dto.getWalletUidTo())
                .build();
    }

    private PaymentRequest createPaymentRequest(PaymentRequestInputDto dto){
        PaymentRequest paymentRequest = paymentRequestMapper.toPaymentRequest(dto);
        paymentRequest.setId(UUID.randomUUID());
        LocalDateTime dateTime = LocalDateTime.now();
        paymentRequest.setCreatedAt(dateTime);
        paymentRequest.setExpiredAt(dateTime.plusMinutes(5L));
        paymentRequest.setStatus(Status.NEW);
        return paymentRequest;
    }

    private void checkWalletOwner(PaymentRequest paymentRequest, UUID walletOwnerId){
        if (!Objects.equals(paymentRequest.getProfileUid(), walletOwnerId)){
            throw new AccessDeniedException("Access denied");
        }
    }
}
