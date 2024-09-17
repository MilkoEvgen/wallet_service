package com.milko.wallet_service.processors;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.exceptions.RequestExpiredException;
import com.milko.wallet_service.mapper.TransactionMapper;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.model.WithdrawalRequest;
import com.milko.wallet_service.repository.TransactionRepository;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.TransactionService;
import com.milko.wallet_service.service.WalletService;
import com.milko.wallet_service.service.impl.WithdrawalRequestServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalProcessor extends BasicProcessor{
    private final ShardService shardService;
    private final WalletService walletService;
    private final PaymentRequestService paymentRequestService;
    private final WithdrawalRequestServiceImpl withdrawalRequestService;
    private final TransactionService transactionService;
    private final TransactionManager transactionManager;

    @Override
    public boolean canProcess(RequestType type) {
        return Objects.equals(type, RequestType.WITHDRAWAL);
    }

    @Override
    public PaymentRequestOutputDto processPaymentRequest(PaymentRequestInputDto dto) {
        log.info("in WithdrawalProcessor.processPaymentRequest(), PaymentRequestInputDto = {}", dto);
        DataSource dataSource = shardService.getDataSourceByUuid(dto.getProfileUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            PaymentRequestOutputDto savedRequest = paymentRequestService.create(dto);
            withdrawalRequestService.create(savedRequest.getId(), dto.getProfileUid());
            return savedRequest;
        });
    }

    @Override
    public TransactionOutputDto createTransaction(PaymentRequestOutputDto paymentRequest) {
        log.info("in WithdrawalProcessor.createTransaction(), paymentRequest = {}", paymentRequest);
        validateRequest(paymentRequest);
        DataSource dataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            WalletOutputDto wallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
            validateFundsForWithdrawal(paymentRequest, wallet);
            Transaction transaction = createTransaction(paymentRequest, RequestType.WITHDRAWAL, wallet.getWalletType().getCurrencyCode());
            return transactionService.create(transaction);
        });
    }

    @Override
    public TransactionOutputDto confirmTransaction(TransactionOutputDto transaction) {
        log.info("in WithdrawalProcessor.confirmTransaction(), transaction = {}", transaction);
        DataSource dataSource = shardService.getDataSourceByUuid(transaction.getProfileUid());
        return transactionManager.executeInTransaction(List.of(dataSource), context -> {
            WalletOutputDto wallet = walletService.findById(transaction.getWalletUid(), transaction.getProfileUid());
            walletService.withdraw(wallet.getUuid(), transaction.getBalanceOperationAmount(), wallet.getProfileUid());
            return transactionService.complete(transaction.getUuid(), transaction.getProfileUid());
        });
    }

    private void validateRequest(PaymentRequestOutputDto paymentRequest) {
        if (paymentRequest.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RequestExpiredException("Payment request with id " + paymentRequest.getId() + "is expired. Expiration time " + paymentRequest.getExpiredAt(), LocalDateTime.now());
        }
    }

    private void validateFundsForWithdrawal(PaymentRequestOutputDto paymentRequest, WalletOutputDto wallet){
        BigDecimal requiredAmount = paymentRequest.getAmount().add(paymentRequest.getFee());
        if (wallet.getBalance().compareTo(requiredAmount) < 0) {
            throw new LowBalanceException("Current balance " + wallet.getBalance() + " is less than required " + requiredAmount, LocalDateTime.now());
        }
    }
}
