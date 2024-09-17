package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.model.IndividualFeeRule;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.repository.IndividualFeeRuleRepository;
import com.milko.wallet_service.service.FeeService;
import com.milko.wallet_service.sharding.ShardService;
import com.milko.wallet_service.transaction.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {
    private final IndividualFeeRuleRepository individualFeeRepository;
    private final ShardService shardService;

    @Override
    public BigDecimal getFee(PaymentRequest paymentRequest) {
        DataSource dataSource = getDataSource(paymentRequest.getProfileUid());
        IndividualFeeRule feeRule = individualFeeRepository.getByTransactionType(paymentRequest.getType(), dataSource);
        BigDecimal amount = paymentRequest.getAmount();
        BigDecimal percentage = feeRule.getPercentage();
        return amount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }

    private DataSource getDataSource(UUID profileId) {
        TransactionContext context = TransactionContext.get();
        DataSource dataSource = shardService.getDataSourceByUuid(profileId);

        if (context.hasActiveTransaction()) {
            Connection connection = context.getConnection(dataSource);
            return new SingleConnectionDataSource(connection, false);
        } else {
            return dataSource;
        }
    }
}
