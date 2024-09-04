package com.milko.wallet_service.processors;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.mapper.PaymentRequestMapper;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.model.TransactionStatus;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
public abstract class BasicProcessor {
    protected PaymentRequestMapper paymentRequestMapper;
    public abstract boolean canProcess(RequestType type);
    public abstract PaymentRequestOutputDto processPaymentRequest(PaymentRequestInputDto dto);
    public abstract TransactionOutputDto createTransaction(PaymentRequestOutputDto paymentRequest);
    public abstract TransactionOutputDto confirmTransaction(TransactionOutputDto transaction);

    protected Transaction createTransaction(PaymentRequestOutputDto paymentRequest, RequestType requestType, String currencyCode) {
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
