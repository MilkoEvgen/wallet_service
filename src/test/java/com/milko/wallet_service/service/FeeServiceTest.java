package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.model.IndividualFeeRule;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.repository.IndividualFeeRuleRepository;
import com.milko.wallet_service.service.impl.FeeServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class FeeServiceTest {

    @Mock
    private IndividualFeeRuleRepository individualFeeRepository;
    @Mock
    private ShardService shardService;
    @Mock
    private DataSource dataSource;
    @InjectMocks
    private FeeServiceImpl feeService;

    private PaymentRequest paymentRequest;
    private IndividualFeeRule feeRule;




    @BeforeEach
    public void init(){
        paymentRequest = PaymentRequest.builder()
                .profileUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50239"))
                .type(RequestType.TOP_UP)
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .build();
        feeRule = IndividualFeeRule.builder()
                .transactionType(RequestType.TOP_UP)
                .percentage(BigDecimal.valueOf(0.3).setScale(2, RoundingMode.HALF_UP))
                .status(Status.ACTIVE)
                .build();
    }

    @Test
    public void getFeeTest(){
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(individualFeeRepository.getByTransactionType(any(), any())).thenReturn(feeRule);

        BigDecimal result = feeService.getFee(paymentRequest);
        assertThat(result).isEqualTo(BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP));

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(individualFeeRepository).getByTransactionType(any(), any());
    }

    @Test
    public void getFeeTest_FeeRuleNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(individualFeeRepository.getByTransactionType(any(), any())).thenThrow(NotFoundException.class);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> feeService.getFee(paymentRequest));
        assertThat(exception.getClass()).isEqualTo(NotFoundException.class);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(individualFeeRepository).getByTransactionType(any(), any());
    }
}
