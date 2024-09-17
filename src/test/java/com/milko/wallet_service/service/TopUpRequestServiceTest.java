package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.TopUpRequestOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.TopUpRequestMapper;
import com.milko.wallet_service.model.TopUpRequest;
import com.milko.wallet_service.repository.TopUpRequestRepository;
import com.milko.wallet_service.service.impl.TopUpRequestServiceImpl;
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
public class TopUpRequestServiceTest {

    @Mock
    private ShardService shardService;
    @Mock
    private TopUpRequestRepository repository;
    @Mock
    private TopUpRequestMapper mapper;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private TopUpRequestServiceImpl topUpRequestService;

    private TopUpRequestOutputDto topUpRequestOutputDto;
    private TopUpRequest topUpRequest;
    private UUID requestId;
    private UUID paymentRequestUid;
    private UUID profileUid;

    @BeforeEach
    public void init() {
        requestId = UUID.randomUUID();
        paymentRequestUid = UUID.randomUUID();
        profileUid = UUID.randomUUID();

        topUpRequest = TopUpRequest.builder()
                .uuid(requestId)
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestUid)
                .build();

        topUpRequestOutputDto = TopUpRequestOutputDto.builder()
                .uuid(requestId)
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestUid)
                .build();
    }

    @Test
    public void createTopUpRequestTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.create(any(), any())).thenReturn(topUpRequest);
        Mockito.when(mapper.toTopUpRequestOutputDto(any())).thenReturn(topUpRequestOutputDto);

        TopUpRequestOutputDto result = topUpRequestService.create(paymentRequestUid, profileUid);

        assertThat(result.getUuid()).isEqualTo(topUpRequestOutputDto.getUuid());
        assertThat(result.getCreatedAt()).isEqualTo(topUpRequestOutputDto.getCreatedAt());
        assertThat(result.getPaymentRequestUid()).isEqualTo(topUpRequestOutputDto.getPaymentRequestUid());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).create(any(), any());
        Mockito.verify(mapper).toTopUpRequestOutputDto(any());
    }

    @Test
    public void createTopUpRequestShouldThrowExceptionWhenDataSourceFails() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenThrow(new RuntimeException("DataSource not found"));

        assertThatThrownBy(() -> topUpRequestService.create(paymentRequestUid, profileUid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DataSource not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
    }

    @Test
    public void findByIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.findById(any(), any())).thenReturn(topUpRequest);
        Mockito.when(mapper.toTopUpRequestOutputDto(any())).thenReturn(topUpRequestOutputDto);

        TopUpRequestOutputDto result = topUpRequestService.findById(requestId, profileUid);

        assertThat(result.getUuid()).isEqualTo(topUpRequestOutputDto.getUuid());
        assertThat(result.getCreatedAt()).isEqualTo(topUpRequestOutputDto.getCreatedAt());
        assertThat(result.getPaymentRequestUid()).isEqualTo(topUpRequestOutputDto.getPaymentRequestUid());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).findById(any(), any());
        Mockito.verify(mapper).toTopUpRequestOutputDto(any());
    }

    @Test
    public void findByIdShouldThrowNotFoundException() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(repository.findById(any(), any())).thenThrow(new NotFoundException("Top-up request not found", LocalDateTime.now()));

        assertThatThrownBy(() -> topUpRequestService.findById(requestId, profileUid))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Top-up request not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(repository).findById(any(), any());
    }
}