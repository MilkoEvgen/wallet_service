package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.model.Transaction;

import java.util.UUID;

public interface TransactionService {

    TransactionOutputDto confirm(ConfirmRequestInputDto confirmRequestInputDto);

    TransactionOutputDto completeTransaction(ConfirmTransactionInputDto confirmDto);
}
