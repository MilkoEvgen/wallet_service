package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;

public interface ProcessorService {
    PaymentRequestOutputDto createPaymentRequest(PaymentRequestInputDto dto);
    TransactionOutputDto confirmTransaction(ConfirmRequestInputDto confirmRequestInputDto);
    TransactionOutputDto completeTransaction(ConfirmTransactionInputDto confirmDto);
}
