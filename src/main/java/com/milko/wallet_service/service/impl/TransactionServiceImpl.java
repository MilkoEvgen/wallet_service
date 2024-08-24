package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptions.AccessDeniedException;
import com.milko.wallet_service.exceptions.LowBalanceException;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.exceptions.RequestExpiredException;
import com.milko.wallet_service.mapper.TransactionMapper;
import com.milko.wallet_service.model.*;
import com.milko.wallet_service.repository.TransactionRepository;
import com.milko.wallet_service.repository.WalletRepository;
import com.milko.wallet_service.service.*;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final ShardService shardService;
    private final PaymentRequestService paymentRequestService;
    private final TransferRequestService transferRequestService;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private final Map<RequestType, Function<PaymentRequestOutputDto, TransactionOutputDto>> requestProcessors = new EnumMap<>(RequestType.class);

    {
        requestProcessors.put(RequestType.TOP_UP, this::processTopUp);
        requestProcessors.put(RequestType.WITHDRAWAL, this::processWithdrawal);
        requestProcessors.put(RequestType.TRANSFER, this::processTransfer);
    }

    @Override
    public TransactionOutputDto confirm(ConfirmRequestInputDto requestInputDto) {
        log.info("in confirm, requestInputDto = {}", requestInputDto);
        PaymentRequestOutputDto paymentRequest = paymentRequestService.findById(requestInputDto.getPaymentRequestId(), requestInputDto.getProfileId());

        validateRequest(requestInputDto, paymentRequest);

        return requestProcessors.getOrDefault(paymentRequest.getType(), pr -> {
            throw new IllegalArgumentException("Unsupported request type: " + pr.getType());
        }).apply(paymentRequest);
    }

    @Override
    public TransactionOutputDto completeTransaction(ConfirmTransactionInputDto confirmDto) {
        log.info("in completeTransaction, confirmDto = {}", confirmDto);

        DataSource dataSource = shardService.getDataSourceByUuid(confirmDto.getProfileId());
        Transaction transaction = transactionRepository.findById(confirmDto.getTransactionId(), dataSource)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        PaymentRequestOutputDto paymentRequest = paymentRequestService.findById(transaction.getPaymentRequestUid(), confirmDto.getProfileId());

        WalletOutputDto wallet = walletService.findById(transaction.getWalletUid(), confirmDto.getProfileId());

        if (paymentRequest.getType().equals(RequestType.TOP_UP)){
            walletService.topUp(wallet.getUuid(), transaction.getBalanceOperationAmount(), wallet.getProfileUid());
        } else if (paymentRequest.getType().equals(RequestType.WITHDRAWAL)){
            walletService.withdraw(wallet.getUuid(), transaction.getBalanceOperationAmount(), wallet.getProfileUid());
        } else if (paymentRequest.getType().equals(RequestType.TRANSFER)){
            //если кошельки на разных шардах - сделать откатную транзакцию
            TransferRequest transferRequest = transferRequestService.findByPaymentRequestId(paymentRequest.getId(), confirmDto.getProfileId());
            WalletOutputDto recipientWallet = walletService.findById(transferRequest.getWalletUidTo(), transferRequest.getRecipientUid());
            walletService.withdraw(wallet.getUuid(), transaction.getBalanceOperationAmount(), wallet.getProfileUid());
            walletService.topUp(recipientWallet.getUuid(), transaction.getRawAmount(), recipientWallet.getProfileUid());

            transactionRepository.updateStatus(transaction.getLinkedTransaction(), TransactionStatus.COMPLETED, dataSource);
        }
        return transactionMapper.toTransactionOutputDto(transactionRepository.updateStatus(transaction.getUuid(), TransactionStatus.COMPLETED, dataSource));
    }

    private void validateRequest(ConfirmRequestInputDto requestInputDto, PaymentRequestOutputDto paymentRequest) {
        if (!Objects.equals(requestInputDto.getProfileId(), paymentRequest.getProfileUid())) {
            throw new AccessDeniedException();
        }
        if (paymentRequest.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RequestExpiredException();
        }
    }

    private TransactionOutputDto processTopUp(PaymentRequestOutputDto paymentRequest) {
        DataSource dataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        WalletOutputDto wallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
        Transaction transaction = createTransaction(paymentRequest, RequestType.TOP_UP, wallet.getWalletType().getCurrencyCode());
        return transactionMapper.toTransactionOutputDto(transactionRepository.create(transaction, dataSource));
    }

    private TransactionOutputDto processWithdrawal(PaymentRequestOutputDto paymentRequest) {
        DataSource dataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        WalletOutputDto wallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
        validateFundsForWithdrawal(paymentRequest, wallet);

        Transaction transaction = createTransaction(paymentRequest, RequestType.WITHDRAWAL, wallet.getWalletType().getCurrencyCode());
        return transactionMapper.toTransactionOutputDto(transactionRepository.create(transaction, dataSource));
    }

    //если кошельки лежат в разных шардах - нужно делать откатую транзакцию
    private TransactionOutputDto processTransfer(PaymentRequestOutputDto paymentRequest) {
        DataSource dataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        WalletOutputDto wallet = walletService.findById(paymentRequest.getOwnerWalletUid(), paymentRequest.getProfileUid());
        validateFundsForWithdrawal(paymentRequest, wallet);

        TransferRequest transferRequest = transferRequestService.findByPaymentRequestId(paymentRequest.getId(), paymentRequest.getProfileUid());

        Transaction recipientTransaction = createTransactionForRecipient(paymentRequest, transferRequest, wallet.getWalletType().getCurrencyCode());
        transactionRepository.create(recipientTransaction, shardService.getDataSourceByUuid(transferRequest.getRecipientUid()));

        Transaction senderTransaction = createTransaction(paymentRequest, RequestType.WITHDRAWAL, wallet.getWalletType().getCurrencyCode());
        senderTransaction.setLinkedTransaction(recipientTransaction.getUuid());
        transactionRepository.create(senderTransaction, dataSource);

        return transactionMapper.toTransactionOutputDto(senderTransaction);
    }

    private void validateFundsForWithdrawal(PaymentRequestOutputDto paymentRequest, WalletOutputDto wallet){
        BigDecimal requiredAmount = paymentRequest.getAmount().add(paymentRequest.getFee());
        if (wallet.getBalance().compareTo(requiredAmount) < 0) {
            throw new LowBalanceException("Current balance " + wallet.getBalance() + " is less than required " + requiredAmount);
        }
    }

    private Transaction createTransactionForRecipient(PaymentRequestOutputDto paymentRequest, TransferRequest transferRequest, String currencyCode) {
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

    private Transaction createTransaction(PaymentRequestOutputDto paymentRequest, RequestType requestType, String currencyCode) {
        BigDecimal balanceAmount = calculateBalanceAmount(requestType, paymentRequest.getAmount(), paymentRequest.getFee());
        return Transaction.builder()
                .uuid(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .profileUid(paymentRequest.getProfileUid())
                .walletUid(paymentRequest.getOwnerWalletUid())
                .balanceOperationAmount(balanceAmount)
                .rawAmount(paymentRequest.getAmount())
                .fee(paymentRequest.getFee())
                .amountInUsd(BigDecimal.ZERO)
                .type(requestType.toString())
                .paymentRequestUid(paymentRequest.getId())
                .currencyCode(currencyCode)
                .refundFee(0L)
                .status(TransactionStatus.CREATED)
                .build();
    }

    private BigDecimal calculateBalanceAmount(RequestType requestType, BigDecimal amount, BigDecimal fee) {
        if (requestType == RequestType.TOP_UP) {
            return amount.subtract(fee);
        } else if (requestType == RequestType.WITHDRAWAL) {
            return amount.add(fee);
        }
        throw new IllegalArgumentException("Unsupported request type: " + requestType);
    }
}
