package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.processors.BasicProcessor;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.ProcessorService;
import com.milko.wallet_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorServiceImpl implements ProcessorService {
    private final PaymentRequestService paymentRequestService;
    private final TransactionService transactionService;
    private final List<BasicProcessor> processors;

    @Override
    public PaymentRequestOutputDto createPaymentRequest(PaymentRequestInputDto dto) {
        log.info("in createPaymentRequest, PaymentRequest = {}", dto);
        return processRequest(dto.getType(), processor -> processor.processPaymentRequest(dto));
    }

    @Override
    public TransactionOutputDto confirmTransaction(ConfirmRequestInputDto requestInputDto) {
        log.info("in confirm, requestInputDto = {}", requestInputDto);
        PaymentRequestOutputDto paymentRequest = paymentRequestService.findById(requestInputDto.getPaymentRequestId(), requestInputDto.getProfileId());

        return processRequest(paymentRequest.getType(), processor -> processor.createTransaction(paymentRequest));
    }

    @Override
    public TransactionOutputDto completeTransaction(ConfirmTransactionInputDto confirmDto) {
        log.info("in completeTransaction, confirmDto = {}", confirmDto);
        TransactionOutputDto transaction = transactionService.findById(confirmDto.getTransactionId(), confirmDto.getProfileId());

        PaymentRequestOutputDto paymentRequest = paymentRequestService.findById(transaction.getPaymentRequestUid(), confirmDto.getProfileId());

        return processRequest(paymentRequest.getType(), processor -> processor.confirmTransaction(transaction));
    }

    private <R> R processRequest(RequestType type, Function<BasicProcessor, R> processorFunction) {
        return processors.stream()
                .filter(processor -> processor.canProcess(type))
                .findFirst()
                .map(processorFunction)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported request type: " + type));
    }


}
