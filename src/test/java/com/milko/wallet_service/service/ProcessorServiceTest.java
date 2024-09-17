package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ConfirmRequestInputDto;
import com.milko.wallet_service.dto.input.ConfirmTransactionInputDto;
import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.model.PaymentRequest;
import com.milko.wallet_service.processors.BasicProcessor;
import com.milko.wallet_service.service.impl.ProcessorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ProcessorServiceTest {
    @Mock
    private PaymentRequestService paymentRequestService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private List<BasicProcessor> processors;
    @Mock
    private BasicProcessor processor;
    @InjectMocks
    private ProcessorServiceImpl processorService;

    private PaymentRequestInputDto requestInputDto;
    private PaymentRequest paymentRequest;
    private PaymentRequestOutputDto requestOutputDto;
    private ConfirmRequestInputDto confirmRequest;
    private TransactionOutputDto transaction;
    private ConfirmTransactionInputDto confirmDto;

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
        confirmRequest = ConfirmRequestInputDto.builder()
                .paymentRequestId(paymentRequest.getId())
                .profileId(requestInputDto.getProfileUid())
                .build();
        transaction = TransactionOutputDto.builder()
                .uuid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50230"))
                .createdAt(LocalDateTime.of(2024, 9, 16, 20, 30))
                .profileUid(requestInputDto.getProfileUid())
                .walletUid(requestOutputDto.getOwnerWalletUid())
                .rawAmount(requestInputDto.getAmount())
                .paymentRequestUid(paymentRequest.getId())
                .build();
        confirmDto = ConfirmTransactionInputDto.builder()
                .profileId(requestInputDto.getProfileUid())
                .transactionId(transaction.getUuid())
                .build();
    }

    @Test
    public void createPaymentRequestTest() {
        Mockito.when(processors.stream())
                .thenReturn(List.of(processor).stream());
        Mockito.when(processor.canProcess(any()))
                .thenReturn(true);
        Mockito.when(processor.processPaymentRequest(any()))
                .thenReturn(requestOutputDto);

        PaymentRequestOutputDto result = processorService.createPaymentRequest(requestInputDto);

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

        Mockito.verify(processor).processPaymentRequest(any());
    }

    @Test
    public void createPaymentRequestShouldThrowIllegalArgumentExceptionWhenTypeNotSupported() {
        Mockito.when(processors.stream())
                .thenReturn(List.of(processor).stream());
        Mockito.when(processor.canProcess(any()))
                .thenReturn(false);

        assertThatThrownBy(() -> processorService.createPaymentRequest(requestInputDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported request type");

        Mockito.verify(processors).stream();
        Mockito.verify(processor).canProcess(any());
    }

    @Test
    public void confirmTransactionTest() {
        Mockito.when(processors.stream()).thenReturn(List.of(processor).stream());
        Mockito.when(processor.canProcess(any())).thenReturn(true);
        Mockito.when(paymentRequestService.findById(any(), any())).thenReturn(requestOutputDto);
        Mockito.when(processor.createTransaction(any())).thenReturn(transaction);

        TransactionOutputDto result = processorService.confirmTransaction(confirmRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(transaction.getUuid());
        assertThat(result.getCreatedAt()).isEqualTo(transaction.getCreatedAt());
        assertThat(result.getProfileUid()).isEqualTo(transaction.getProfileUid());
        assertThat(result.getWalletUid()).isEqualTo(transaction.getWalletUid());
        assertThat(result.getRawAmount()).isEqualTo(transaction.getRawAmount());
        assertThat(result.getPaymentRequestUid()).isEqualTo(transaction.getPaymentRequestUid());

        Mockito.verify(processor).canProcess(any());
        Mockito.verify(paymentRequestService).findById(any(), any());
        Mockito.verify(processor).createTransaction(any());
    }

    @Test
    public void confirmTransactionShouldThrowIllegalArgumentExceptionWhenTypeNotSupported() {
        Mockito.when(paymentRequestService.findById(any(), any())).thenReturn(requestOutputDto);
        Mockito.when(processors.stream()).thenReturn(List.of(processor).stream());
        Mockito.when(processor.canProcess(any())).thenReturn(false);

        assertThatThrownBy(() -> processorService.confirmTransaction(confirmRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported request type");

        Mockito.verify(paymentRequestService).findById(any(), any());
        Mockito.verify(processors).stream();
        Mockito.verify(processor).canProcess(any());
    }

    @Test
    public void completeTransactionTest() {
        Mockito.when(transactionService.findById(any(), any())).thenReturn(transaction);
        Mockito.when(paymentRequestService.findById(any(), any())).thenReturn(requestOutputDto);
        Mockito.when(processors.stream()).thenReturn(List.of(processor).stream());
        Mockito.when(processor.canProcess(any())).thenReturn(true);
        Mockito.when(processor.confirmTransaction(any())).thenReturn(transaction);

        TransactionOutputDto result = processorService.completeTransaction(confirmDto);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(transaction.getUuid());
        assertThat(result.getCreatedAt()).isEqualTo(transaction.getCreatedAt());
        assertThat(result.getProfileUid()).isEqualTo(transaction.getProfileUid());
        assertThat(result.getWalletUid()).isEqualTo(transaction.getWalletUid());
        assertThat(result.getRawAmount()).isEqualTo(transaction.getRawAmount());
        assertThat(result.getPaymentRequestUid()).isEqualTo(transaction.getPaymentRequestUid());

        Mockito.verify(transactionService).findById(any(), any());
        Mockito.verify(paymentRequestService).findById(any(), any());
        Mockito.verify(processors).stream();
        Mockito.verify(processor).canProcess(any());
        Mockito.verify(processor).confirmTransaction(any());
    }

    @Test
    public void completeTransactionShouldThrowIllegalArgumentExceptionWhenTypeNotSupported() {
        Mockito.when(transactionService.findById(any(), any())).thenReturn(transaction);
        Mockito.when(paymentRequestService.findById(any(), any())).thenReturn(requestOutputDto);
        Mockito.when(processors.stream()).thenReturn(List.of(processor).stream());
        Mockito.when(processor.canProcess(any())).thenReturn(false);

        assertThatThrownBy(() -> processorService.completeTransaction(confirmDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported request type");

        Mockito.verify(transactionService).findById(any(), any());
        Mockito.verify(paymentRequestService).findById(any(), any());
        Mockito.verify(processors).stream();
        Mockito.verify(processor).canProcess(any());
    }

}
