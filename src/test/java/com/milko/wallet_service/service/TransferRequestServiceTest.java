package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.mapper.TransferRequestMapper;
import com.milko.wallet_service.model.TransferRequest;
import com.milko.wallet_service.repository.TransferRequestRepository;
import com.milko.wallet_service.service.impl.TransferRequestServiceImpl;
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
public class TransferRequestServiceTest {

    @Mock
    private TransferRequestRepository requestRepository;
    @Mock
    private TransferRequestMapper mapper;
    @Mock
    private ShardService shardService;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private TransferRequestServiceImpl transferRequestService;

    private TransferRequestOutputDto transferRequestOutputDto;
    private TransferRequest transferRequest;
    private UUID transferRequestId;
    private UUID profileUid;
    private UUID paymentRequestId;
    private UUID recipientUid;
    private UUID walletUidTo;

    @BeforeEach
    public void init() {
        transferRequestId = UUID.randomUUID();
        profileUid = UUID.randomUUID();
        paymentRequestId = UUID.randomUUID();
        recipientUid = UUID.randomUUID();
        walletUidTo = UUID.randomUUID();

        transferRequest = TransferRequest.builder()
                .uuid(transferRequestId)
                .createdAt(LocalDateTime.now())
                .recipientUid(recipientUid)
                .walletUidTo(walletUidTo)
                .build();

        transferRequestOutputDto = TransferRequestOutputDto.builder()
                .uuid(transferRequestId)
                .createdAt(LocalDateTime.now())
                .paymentRequestUid(paymentRequestId)
                .recipientUid(recipientUid)
                .walletUidTo(walletUidTo)
                .build();
    }

    @Test
    public void createTransferRequestTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(requestRepository.create(any(), any())).thenReturn(transferRequest);
        Mockito.when(mapper.toTransferRequestOutputDto(any())).thenReturn(transferRequestOutputDto);

        TransferRequestOutputDto result = transferRequestService.create(transferRequest, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(transferRequestOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(requestRepository).create(any(), any());
        Mockito.verify(mapper).toTransferRequestOutputDto(any());
    }

    @Test
    public void createTransferRequestShouldThrowExceptionWhenDataSourceFails() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenThrow(new RuntimeException("DataSource not found"));

        assertThatThrownBy(() -> transferRequestService.create(transferRequest, profileUid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DataSource not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
    }

    @Test
    public void findByIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(requestRepository.findById(any(), any())).thenReturn(transferRequest);
        Mockito.when(mapper.toTransferRequestOutputDto(any())).thenReturn(transferRequestOutputDto);

        TransferRequestOutputDto result = transferRequestService.findById(transferRequestId, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(transferRequestOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(requestRepository).findById(any(), any());
        Mockito.verify(mapper).toTransferRequestOutputDto(any());
    }

    @Test
    public void findByIdShouldThrowExceptionWhenNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(requestRepository.findById(any(), any())).thenThrow(new RuntimeException("TransferRequest not found"));

        assertThatThrownBy(() -> transferRequestService.findById(transferRequestId, profileUid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TransferRequest not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(requestRepository).findById(any(), any());
    }

    @Test
    public void findByPaymentRequestIdTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(requestRepository.findByPaymentRequestId(any(), any())).thenReturn(transferRequest);
        Mockito.when(mapper.toTransferRequestOutputDto(any())).thenReturn(transferRequestOutputDto);

        TransferRequestOutputDto result = transferRequestService.findByPaymentRequestId(paymentRequestId, profileUid);

        assertThat(result).usingRecursiveComparison().isEqualTo(transferRequestOutputDto);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(requestRepository).findByPaymentRequestId(any(), any());
        Mockito.verify(mapper).toTransferRequestOutputDto(any());
    }

    @Test
    public void findByPaymentRequestIdShouldThrowExceptionWhenNotFound() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(requestRepository.findByPaymentRequestId(any(), any())).thenThrow(new RuntimeException("TransferRequest not found"));

        assertThatThrownBy(() -> transferRequestService.findByPaymentRequestId(paymentRequestId, profileUid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TransferRequest not found");

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(requestRepository).findByPaymentRequestId(any(), any());
    }
}
