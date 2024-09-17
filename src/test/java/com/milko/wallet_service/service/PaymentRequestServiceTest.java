package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransferRequestOutputDto;
import com.milko.wallet_service.exceptions.NotFoundException;
import com.milko.wallet_service.mapper.PaymentRequestMapper;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.repository.PaymentRequestRepository;
import com.milko.wallet_service.service.impl.PaymentRequestServiceImpl;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class PaymentRequestServiceTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;
    @Mock
    private TransferRequestService transferRequestService;
    @Mock
    private ShardService shardService;
    @Mock
    private PaymentRequestMapper paymentRequestMapper;
    @Mock
    private FeeService feeService;
    @Mock
    DataSource dataSource;

    @InjectMocks
    private PaymentRequestServiceImpl paymentRequestService;

    private PaymentRequestInputDto requestInputDto;
    private PaymentRequest paymentRequest;
    private PaymentRequestOutputDto requestOutputDto;
    private TransferRequestOutputDto transferRequest;

    @BeforeEach
    public void init() {
        requestInputDto = PaymentRequestInputDto.builder()
                .profileUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50230"))
                .ownerWalletUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50231"))
                .recipientUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50232"))
                .walletUidTo(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50233"))
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .type(RequestType.TRANSFER)
                .build();
        paymentRequest = PaymentRequest.builder()
                .id(UUID.randomUUID())
                .profileUid(requestInputDto.getProfileUid())
                .ownerWalletUid(requestInputDto.getOwnerWalletUid())
                .amount(requestInputDto.getAmount())
                .type(requestInputDto.getType())
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .status(Status.NEW)
                .build();
        requestOutputDto = PaymentRequestOutputDto.builder()
                .id(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50230"))
                .createdAt(LocalDateTime.of(2024, 9, 16, 20, 30))
                .expiredAt(LocalDateTime.of(2024, 9, 16, 20, 35))
                .profileUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50230"))
                .ownerWalletUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50231"))
                .recipientUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50232"))
                .walletUidTo(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50233"))
                .amount(BigDecimal.valueOf(130).setScale(2, RoundingMode.HALF_UP))
                .fee(BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP))
                .type(RequestType.TRANSFER)
                .status(Status.ACTIVE)
                .build();
        transferRequest = TransferRequestOutputDto.builder()
                .recipientUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50232"))
                .walletUidTo(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50233"))
                .build();
    }

    @Test
    public void createPaymentRequestTest() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(paymentRequestMapper.toPaymentRequest(any())).thenReturn(paymentRequest);
        BigDecimal fee = BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP);
        Mockito.when(feeService.getFee(any())).thenReturn(fee);
        paymentRequest.setFee(fee);
        Mockito.when(paymentRequestRepository.create(any(), any())).thenReturn(paymentRequest);
        Mockito.when(paymentRequestMapper.toPaymentRequestOutputDto(any())).thenReturn(requestOutputDto);

        PaymentRequestOutputDto result = paymentRequestService.create(requestInputDto);

        assertThat(result.getId()).isEqualTo(requestOutputDto.getId());
        assertThat(result.getCreatedAt()).isEqualTo(requestOutputDto.getCreatedAt());
        assertThat(result.getExpiredAt()).isEqualTo(requestOutputDto.getExpiredAt());
        assertThat(result.getProfileUid()).isEqualTo(requestOutputDto.getProfileUid());
        assertThat(result.getOwnerWalletUid()).isEqualTo(requestOutputDto.getOwnerWalletUid());
        assertThat(result.getRecipientUid()).isEqualTo(requestOutputDto.getRecipientUid());
        assertThat(result.getWalletUidTo()).isEqualTo(requestOutputDto.getWalletUidTo());
        assertThat(result.getAmount()).isEqualByComparingTo(requestOutputDto.getAmount());
        assertThat(result.getFee()).isEqualByComparingTo(requestOutputDto.getFee());
        assertThat(result.getType()).isEqualTo(requestOutputDto.getType());
        assertThat(result.getStatus()).isEqualTo(requestOutputDto.getStatus());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(paymentRequestMapper).toPaymentRequest(any());
        Mockito.verify(feeService).getFee(any());
        Mockito.verify(paymentRequestRepository).create(any(), any());
        Mockito.verify(paymentRequestMapper).toPaymentRequestOutputDto(any());
    }

    @Test
    public void createPaymentRequestShouldThrowNotFoundException() {
        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(paymentRequestMapper.toPaymentRequest(any())).thenReturn(paymentRequest);
        Mockito.when(feeService.getFee(any())).thenThrow(NotFoundException.class);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentRequestService.create(requestInputDto));
        assertThat(exception.getClass()).isEqualTo(NotFoundException.class);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(paymentRequestMapper).toPaymentRequest(any());
        Mockito.verify(feeService).getFee(any());
    }

    @Test
    public void findByIdShouldReturnPaymentRequest() {
        UUID requestId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(paymentRequestRepository.findById(any(), any())).thenReturn(paymentRequest);
        Mockito.when(paymentRequestMapper.toPaymentRequestOutputDto(any())).thenReturn(requestOutputDto);
        Mockito.when(transferRequestService.findByPaymentRequestId(any(), any())).thenReturn(transferRequest);

        PaymentRequestOutputDto result = paymentRequestService.findById(requestId, profileId);

        assertThat(result.getId()).isEqualTo(requestOutputDto.getId());
        assertThat(result.getCreatedAt()).isEqualTo(requestOutputDto.getCreatedAt());
        assertThat(result.getExpiredAt()).isEqualTo(requestOutputDto.getExpiredAt());
        assertThat(result.getProfileUid()).isEqualTo(requestOutputDto.getProfileUid());
        assertThat(result.getOwnerWalletUid()).isEqualTo(requestOutputDto.getOwnerWalletUid());
        assertThat(result.getRecipientUid()).isEqualTo(requestOutputDto.getRecipientUid());
        assertThat(result.getWalletUidTo()).isEqualTo(requestOutputDto.getWalletUidTo());
        assertThat(result.getAmount()).isEqualByComparingTo(requestOutputDto.getAmount());
        assertThat(result.getFee()).isEqualByComparingTo(requestOutputDto.getFee());
        assertThat(result.getType()).isEqualTo(requestOutputDto.getType());
        assertThat(result.getStatus()).isEqualTo(requestOutputDto.getStatus());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(paymentRequestRepository).findById(any(), any());
        Mockito.verify(paymentRequestMapper).toPaymentRequestOutputDto(any());
        Mockito.verify(transferRequestService).findByPaymentRequestId(any(), any());
    }

    @Test
    public void findByIdShouldThrowNotFoundException() {
        UUID requestId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(paymentRequestRepository.findById(any(), any())).thenThrow(NotFoundException.class);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentRequestService.findById(requestId, profileId));
        assertThat(exception.getClass()).isEqualTo(NotFoundException.class);

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(paymentRequestRepository).findById(any(), any());
    }

    @Test
    public void findAllByUserIdShouldReturnListOfPaymentRequests() {
        UUID profileId = UUID.randomUUID();
        List<PaymentRequest> paymentRequests = Collections.singletonList(paymentRequest);
        List<PaymentRequestOutputDto> paymentRequestOutputDtos = Collections.singletonList(requestOutputDto);

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(paymentRequestRepository.findAllByUserId(any(), any())).thenReturn(paymentRequests);
        Mockito.when(paymentRequestMapper.toPaymentRequestOutputDtoList(any())).thenReturn(paymentRequestOutputDtos);

        List<PaymentRequestOutputDto> result = paymentRequestService.findAllByUserId(profileId);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);

        PaymentRequestOutputDto outputDto = result.get(0);
        assertThat(outputDto.getId()).isEqualTo(requestOutputDto.getId());
        assertThat(outputDto.getCreatedAt()).isEqualTo(requestOutputDto.getCreatedAt());
        assertThat(outputDto.getExpiredAt()).isEqualTo(requestOutputDto.getExpiredAt());
        assertThat(outputDto.getProfileUid()).isEqualTo(requestOutputDto.getProfileUid());
        assertThat(outputDto.getOwnerWalletUid()).isEqualTo(requestOutputDto.getOwnerWalletUid());
        assertThat(outputDto.getRecipientUid()).isEqualTo(requestOutputDto.getRecipientUid());
        assertThat(outputDto.getWalletUidTo()).isEqualTo(requestOutputDto.getWalletUidTo());
        assertThat(outputDto.getAmount()).isEqualByComparingTo(requestOutputDto.getAmount());
        assertThat(outputDto.getFee()).isEqualByComparingTo(requestOutputDto.getFee());
        assertThat(outputDto.getType()).isEqualTo(requestOutputDto.getType());
        assertThat(outputDto.getStatus()).isEqualTo(requestOutputDto.getStatus());

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(paymentRequestRepository).findAllByUserId(any(), any());
        Mockito.verify(paymentRequestMapper).toPaymentRequestOutputDtoList(any());
    }

    @Test
    public void findAllByUserIdShouldReturnEmptyList() {
        UUID profileId = UUID.randomUUID();

        Mockito.when(shardService.getDataSourceByUuid(any())).thenReturn(dataSource);
        Mockito.when(paymentRequestRepository.findAllByUserId(any(), any())).thenReturn(Collections.emptyList());

        List<PaymentRequestOutputDto> result = paymentRequestService.findAllByUserId(profileId);

        assertThat(result).isEmpty();

        Mockito.verify(shardService).getDataSourceByUuid(any());
        Mockito.verify(paymentRequestRepository).findAllByUserId(any(), any());
    }

}
