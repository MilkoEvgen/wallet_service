package com.milko.wallet_service.processors;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.exceptions.RequestExpiredException;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.model.TransferRequest;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.TransactionService;
import com.milko.wallet_service.service.TransferRequestService;
import com.milko.wallet_service.service.WalletService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
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
public class TransferProcessor extends BasicProcessor{
    private final ShardService shardService;
    private final WalletService walletService;
    private final PaymentRequestService paymentRequestService;
    private final TransferRequestService transferRequestService;
    private final TransactionService transactionService;
    private final TransactionManager transactionManager;

    @Override
    public boolean canProcess(RequestType type) {
        return Objects.equals(type, RequestType.TRANSFER);
    }

    @Override
    public PaymentRequestOutputDto processPaymentRequest(PaymentRequestInputDto dto) {
        log.info("in TransferProcessor.processPaymentRequest(), PaymentRequestInputDto = {}", dto);
        DataSource mainDataSource = shardService.getDataSourceByUuid(dto.getProfileUid());
        DataSource alternativeDataSource = shardService.getDataSourceByUuid(dto.getRecipientUid());

        return transactionManager.executeInTransaction(List.of(mainDataSource, alternativeDataSource), context -> {
            WalletOutputDto senderWallet = walletService.findById(dto.getOwnerWalletUid(), dto.getProfileUid());
            PaymentRequestOutputDto paymentRequest = paymentRequestService.create(dto);

            WalletOutputDto recipientWallet = walletService.findById(dto.getWalletUidTo(), dto.getRecipientUid());
            if (!Objects.equals(senderWallet.getWalletType().getCurrencyCode(), recipientWallet.getWalletType().getCurrencyCode())){
                throw new RuntimeException("Wallet currencies do not match");
            }

            TransferRequestOutputDto transferRequest = transferRequestService.create(createTransferRequest(paymentRequest, dto), dto.getProfileUid());
            paymentRequest.setWalletUidTo(transferRequest.getWalletUidTo());
            return paymentRequest;
        });


    }

    @Override
    public TransactionOutputDto createTransaction(PaymentRequestOutputDto paymentRequest) {
        log.info("in TransferProcessor.createTransaction(), paymentRequest = {}", paymentRequest);
        validateRequest(paymentRequest);
        DataSource mainDataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        DataSource alternativeDataSource = shardService.getDataSourceByUuid(paymentRequest.getRecipientUid());

        return transactionManager.executeInTransaction(List.of(mainDataSource, alternativeDataSource), context -> {
            WalletOutputDto wallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
            validateFundsForWithdrawal(paymentRequest, wallet);

            TransferRequestOutputDto transferRequest = transferRequestService.findByPaymentRequestId(paymentRequest.getId(), paymentRequest.getProfileUid());

            Transaction recipientTransaction = createTransactionForRecipient(paymentRequest, transferRequest, wallet.getWalletType().getCurrencyCode());
            transactionService.create(recipientTransaction);

            Transaction senderTransaction = createTransaction(paymentRequest, RequestType.WITHDRAWAL, wallet.getWalletType().getCurrencyCode());
            senderTransaction.setLinkedTransaction(recipientTransaction.getUuid());
            return transactionService.create(senderTransaction);
        });
    }

    @Override
    public TransactionOutputDto confirmTransaction(TransactionOutputDto transaction) {
        log.info("in TransferProcessor.confirmTransaction(), transaction = {}", transaction);
        DataSource mainDataSource = shardService.getDataSourceByUuid(transaction.getProfileUid());

        return transactionManager.executeInTransaction(List.of(mainDataSource), context -> {
            WalletOutputDto senderWallet = walletService.findById(transaction.getWalletUid(), transaction.getProfileUid());
            TransferRequestOutputDto transferRequest = transferRequestService.findByPaymentRequestId(transaction.getPaymentRequestUid(), transaction.getProfileUid());

            DataSource alternativeDataSource = shardService.getDataSourceByUuid(transferRequest.getRecipientUid());
            transactionManager.addDataSourceToTransaction(alternativeDataSource);

            WalletOutputDto recipientWallet = walletService.findById(transferRequest.getWalletUidTo(), transferRequest.getRecipientUid());
            walletService.withdraw(senderWallet.getUuid(), transaction.getBalanceOperationAmount(), senderWallet.getProfileUid());
            walletService.topUp(recipientWallet.getUuid(), transaction.getRawAmount(), recipientWallet.getProfileUid());

            transactionService.complete(transaction.getLinkedTransaction(), transferRequest.getRecipientUid());
            return transactionService.complete(transaction.getUuid(), transaction.getProfileUid());
        });
    }

    private TransferRequest createTransferRequest(PaymentRequestOutputDto paymentRequest, PaymentRequestInputDto dto){
        return TransferRequest.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequest.getId())
                .recipientUid(dto.getRecipientUid())
                .walletUidTo(dto.getWalletUidTo())
                .build();
    }

    private void validateRequest(PaymentRequestOutputDto paymentRequest) {
        if (paymentRequest.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RequestExpiredException();
        }
    }

    private void validateFundsForWithdrawal(PaymentRequestOutputDto paymentRequest, WalletOutputDto wallet){
        BigDecimal requiredAmount = paymentRequest.getAmount().add(paymentRequest.getFee());
        if (wallet.getBalance().compareTo(requiredAmount) < 0) {
            throw new LowBalanceException("Current balance " + wallet.getBalance() + " is less than required " + requiredAmount);
        }
    }

    private Transaction createTransactionForRecipient(PaymentRequestOutputDto paymentRequest, TransferRequestOutputDto transferRequest, String currencyCode) {
        return Transaction.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .profileUid(transferRequest.getRecipientUid())
                .walletUid(transferRequest.getWalletUidTo())
                .balanceOperationAmount(paymentRequest.getAmount())
                .rawAmount(paymentRequest.getAmount())
                .fee(BigDecimal.ZERO)
                .amountInUsd(BigDecimal.ZERO)
                .type(RequestType.TOP_UP.toString())
                .paymentRequestUid(paymentRequest.getId())
                .currencyCode(currencyCode)
                .refundFee(0L)
                .status(TransactionStatus.CREATED)
                .build();
    }
}
