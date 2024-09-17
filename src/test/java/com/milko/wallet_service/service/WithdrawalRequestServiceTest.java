package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.WithdrawalRequestOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.WithdrawalRequestMapper;
import com.milko.wallet_service.model.WithdrawalRequest;
import com.milko.wallet_service.repository.WithdrawalRequestRepository;
import com.milko.wallet_service.service.impl.WithdrawalRequestServiceImpl;
import com.milko.wallet_service.sharding.ShardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class WithdrawalRequestServiceTest {

    @Mock
    private ShardService shardService;
    @Mock
    private WithdrawalRequestRepository repository;
    @Mock
    private WithdrawalRequestMapper mapper;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private WithdrawalRequestServiceImpl withdrawalRequestService;

    private WithdrawalRequestOutputDto withdrawalRequestOutputDto;
    private WithdrawalRequest withdrawalRequest;
    private UUID requestId;
    private UUID paymentRequestUid;
    private UUID profileUid;

    @BeforeEach
    public void init() {
        requestId = UUID.randomUUID();
        paymentRequestUid = UUID.randomUUID();
        profileUid = UUID.randomUUID();

        withdrawalRequest = WithdrawalRequest.builder()
                .uuid(requestId)
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestUid)
                .build();

        withdrawalRequestOutputDto = WithdrawalRequestOutputDto.builder()
                .uuid(requestId)
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestUid)
                .build();
    }

    @Test
    public void createWithdrawalRequestTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.create(any(), any())).thenReturn(withdrawalRequest);
        Mockito.when(mapper.toWithdrawalRequestOutputDto(any())).thenReturn(withdrawalRequestOutputDto);

        WithdrawalRequestOutputDto result = withdrawalRequestService.create(paymentRequestUid, profileUid);

        assertThat(result.getUuid()).isEqualTo(withdrawalRequestOutputDto.getUuid());
        assertThat(result.getCreatedAt()).isEqualTo(withdrawalRequestOutputDto.getCreatedAt());
        assertThat(result.getPaymentRequestUid()).isEqualTo(withdrawalRequestOutputDto.getPaymentRequestUid());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).create(any(), any());
        Mockito.verify(mapper).toWithdrawalRequestOutputDto(any());
    }

    @Test
    public void createWithdrawalRequestShouldThrowExceptionWhenDataSourceFails() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenThrow(new RuntimeException("DataSource not found"));

        assertThatThrownBy(() -> withdrawalRequestService.create(paymentRequestUid, profileUid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DataSource not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
    }

    @Test
    public void findByIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.findById(any(), any())).thenReturn(withdrawalRequest);
        Mockito.when(mapper.toWithdrawalRequestOutputDto(any())).thenReturn(withdrawalRequestOutputDto);

        WithdrawalRequestOutputDto result = withdrawalRequestService.findById(requestId, profileUid);

        assertThat(result.getUuid()).isEqualTo(withdrawalRequestOutputDto.getUuid());
        assertThat(result.getCreatedAt()).isEqualTo(withdrawalRequestOutputDto.getCreatedAt());
        assertThat(result.getPaymentRequestUid()).isEqualTo(withdrawalRequestOutputDto.getPaymentRequestUid());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).findById(any(), any());
        Mockito.verify(mapper).toWithdrawalRequestOutputDto(any());
    }

    @Test
    public void findByIdShouldThrowNotFoundException() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.findById(any(), any())).thenThrow(new NotFoundException("Withdrawal request not found", LocalDateTime.now()));

        assertThatThrownBy(() -> withdrawalRequestService.findById(requestId, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Withdrawal request not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).findById(any(), any());
    }
}
