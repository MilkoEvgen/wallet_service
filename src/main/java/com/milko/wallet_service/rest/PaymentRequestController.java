package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.mapper.TransactionMapper;
import com.milko.wallet_service.model.Transaction;
import com.milko.wallet_service.service.PaymentRequestService;
import com.milko.wallet_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentRequestController {
    private final PaymentRequestService paymentRequestService;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    //    нужно сравнивать валюты исходного кошелька и получателя (при переводе)
    @PostMapping("request")
    public PaymentRequestOutputDto createPaymentRequest(@RequestBody PaymentRequestInputDto requestInputDto){
        log.info("in createPaymentRequest, requestInputDto = {}", requestInputDto);
        return paymentRequestService.create(requestInputDto);
    }

    //    эндпоинт: подтвердить перевод (включает в себя id payment_request)
    //    на этот запрос уже создается транзакция в статусе CREATED (возвращаем id и инфу по транзакции)
    @PostMapping("transaction")
    public TransactionOutputDto createTransaction(@RequestBody ConfirmRequestInputDto confirmRequestInputDto){
        log.info("in createTransaction, confirmRequestInputDto = {}", confirmRequestInputDto);
        return transactionService.confirm(confirmRequestInputDto);
    }

    //    эндпоинт: подтверждение транзакции, меняем ей статус
    @PostMapping("confirm")
    public TransactionOutputDto confirmTransaction(@RequestBody ConfirmTransactionInputDto confirmDto){
        return transactionService.completeTransaction(confirmDto);
    }
}
