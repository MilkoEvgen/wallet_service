package com.milko.wallet_service.repository;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.model.IndividualFeeRule;

import javax.sql.DataSource;
import java.util.Optional;

public interface IndividualFeeRuleRepository {
    IndividualFeeRule create(IndividualFeeRule feeRule, DataSource dataSource);
    IndividualFeeRule getByTransactionType(RequestType transactionType, DataSource dataSource);
    IndividualFeeRule getById(Long id, DataSource dataSource);
}
