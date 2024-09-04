package com.milko.wallet_service.processors;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptions.RequestExpiredException;
import com.milko.wallet_service.mapper.TransactionMapper;
import com.milko.wallet_service.model.TopUpRequest;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.repository.TopUpRequestRepository;
import com.milko.wallet_service.repository.TransactionRepository;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.TransactionService;
import com.milko.wallet_service.service.WalletService;
import com.milko.wallet_service.service.impl.TopUpRequestServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpProcessor extends BasicProcessor {
    private final ShardService shardService;
    private final WalletService walletService;
    private final PaymentRequestService paymentRequestService;
    private final TopUpRequestServiceImpl topUpRequestService;
    private final TransactionService transactionService;
    private final TransactionManager transactionManager;


    @Override
    public boolean canProcess(RequestType type) {
        return Objects.equals(type, RequestType.TOP_UP);
    }

    @Override
    public PaymentRequestOutputDto processPaymentRequest(PaymentRequestInputDto dto) {
        log.info("in TopUpProcessor.processPaymentRequest(), PaymentRequestInputDto = {}", dto);
        DataSource dataSource = shardService.getDataSourceByUuid(dto.getProfileUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            PaymentRequestOutputDto savedRequest = paymentRequestService.create(dto);
            topUpRequestService.create(savedRequest.getId(), dto.getProfileUid());
            return savedRequest;
        });
    }

    @Override
    public TransactionOutputDto createTransaction(PaymentRequestOutputDto paymentRequest) {
        log.info("in TopUpProcessor.createTransaction(), paymentRequest = {}", paymentRequest);
        validateRequest(paymentRequest);
        DataSource dataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            WalletOutputDto wallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
            Transaction transaction = createTransaction(paymentRequest, RequestType.TOP_UP, wallet.getWalletType().getCurrencyCode());
            return transactionService.create(transaction);
        });

    }

    @Override
    public TransactionOutputDto confirmTransaction(TransactionOutputDto transaction) {
        log.info("in TopUpProcessor.confirmTransaction(), transaction = {}", transaction);
        DataSource dataSource = shardService.getDataSourceByUuid(transaction.getProfileUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            WalletOutputDto wallet = walletService.findById(transaction.getWalletUid(), transaction.getProfileUid());
            walletService.topUp(wallet.getUuid(), transaction.getBalanceOperationAmount(), wallet.getProfileUid());
            return transactionService.complete(transaction.getUuid(), transaction.getProfileUid());
        });
    }


    private void validateRequest(PaymentRequestOutputDto paymentRequest) {
        if (paymentRequest.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RequestExpiredException();
        }
    }
}
