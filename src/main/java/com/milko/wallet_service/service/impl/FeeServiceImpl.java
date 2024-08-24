package com.milko.wallet_service.service.impl;

import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.model.IndividualFeeRule;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.repository.IndividualFeeRuleRepository;
import com.milko.wallet_service.service.FeeService;
import com.milko.wallet_service.sharding.ShardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {
    private final IndividualFeeRuleRepository individualFeeRepository;
    private final ShardService shardService;

    @Override
    public BigDecimal getFee(PaymentRequest paymentRequest) {
        DataSource dataSource = shardService.getDataSourceByUuid(paymentRequest.getProfileUid());
        IndividualFeeRule feeRule = individualFeeRepository.getByTransactionType(paymentRequest.getType(), dataSource)
                .orElseThrow(() -> new NotFoundException("Fee rule not found"));
        BigDecimal amount = paymentRequest.getAmount();
        BigDecimal percentage = feeRule.getPercentage();
        return amount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    }
}
