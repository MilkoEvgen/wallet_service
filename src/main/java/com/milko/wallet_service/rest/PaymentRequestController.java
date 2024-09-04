package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.service.ProcessorService;
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
    private final ProcessorService processorService;


    @PostMapping("request")
    public PaymentRequestOutputDto createPaymentRequest(@RequestBody PaymentRequestInputDto requestInputDto){
        log.info("in createPaymentRequest, requestInputDto = {}", requestInputDto);
        return processorService.createPaymentRequest(requestInputDto);
    }

    @PostMapping("transaction")
    public TransactionOutputDto createTransaction(@RequestBody ConfirmRequestInputDto confirmRequestInputDto){
        log.info("in createTransaction, confirmRequestInputDto = {}", confirmRequestInputDto);
        return processorService.confirmTransaction(confirmRequestInputDto);
    }

    @PostMapping("confirm")
    public TransactionOutputDto confirmTransaction(@RequestBody ConfirmTransactionInputDto confirmDto){
        return processorService.completeTransaction(confirmDto);
    }
}
