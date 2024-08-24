package com.milko.wallet_service.mapper;

import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionOutputDto toTransactionOutputDto(Transaction transaction);
}
