package com.milko.wallet_service.testcontainers;

import com.milko.wallet_service.config.JdbcTemplateTestConfig;
import com.milko.wallet_service.dto.RequestType;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.*;
import com.milko.wallet_service.dto.output.PaymentRequestOutputDto;
import com.milko.wallet_service.dto.output.TransactionOutputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptionhandling.ErrorResponse;
import com.milko.wallet_service.model.TransactionStatus;
import com.milko.wallet_service.repository.TransactionRepository;
import com.milko.wallet_service.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Import(JdbcTemplateTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class PaymentRequestControllerTest {
    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer1;
    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer2;

    static {
        postgreSQLContainer1 = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName("ds_0")
                .withUsername("test")
                .withPassword("test");

        postgreSQLContainer2 = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName("ds_1")
                .withUsername("test")
                .withPassword("test");
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("datasource.ds0.url", postgreSQLContainer1::getJdbcUrl);
        registry.add("datasource.ds0.user", postgreSQLContainer1::getUsername);
        registry.add("datasource.ds0.password", postgreSQLContainer1::getPassword);

        registry.add("datasource.ds1.url", postgreSQLContainer2::getJdbcUrl);
        registry.add("datasource.ds1.user", postgreSQLContainer2::getUsername);
        registry.add("datasource.ds1.password", postgreSQLContainer2::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    @Qualifier("jdbcTemplateDs0")
    private JdbcTemplate jdbcTemplateDs0;

    @Autowired
    @Qualifier("jdbcTemplateDs1")
    private JdbcTemplate jdbcTemplateDs1;

    @SpyBean
    private TransactionRepository transactionRepository;
    @SpyBean
    private WalletRepository walletRepository;

    WalletTypeInputDto walletTypeInputDto;
    WalletInputDto ownerWalletInputDto;
    WalletInputDto recipientWalletInputDto;
    PaymentRequestInputDto paymentRequestInputDto;
    private String walletTypeUrl;
    private String walletUrl;
    private String requestUrl;

    @BeforeEach
    public void init(){
        walletTypeInputDto = WalletTypeInputDto.builder()
                .name("name")
                .currencyCode("UAH")
                .status(Status.ACTIVE)
                .profileType("type")
                .creator("creator")
                .build();
        ownerWalletInputDto = WalletInputDto.builder()
                .name("owner wallet")
                .profileUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50239"))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(200).setScale(2, RoundingMode.HALF_UP))
                .build();
        recipientWalletInputDto = WalletInputDto.builder()
                .name("recipient wallet")
                .profileUid(UUID.fromString("6f1f9735-16d4-43ac-9284-77f65405cfb0"))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(200).setScale(2, RoundingMode.HALF_UP))
                .build();
        paymentRequestInputDto = PaymentRequestInputDto.builder()
                .profileUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50239"))
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .build();
        walletTypeUrl = "http://localhost:" + port + "/api/v1/wallet_types";
        walletUrl = "http://localhost:" + port + "/api/v1/wallets";
        requestUrl = "http://localhost:" + port + "/api/v1";
    }

    @AfterEach
    public void cleanDatabase(){
        jdbcTemplateDs0.update("DELETE FROM transactions");
        jdbcTemplateDs1.update("DELETE FROM transactions");
        jdbcTemplateDs0.update("DELETE FROM top_up_requests");
        jdbcTemplateDs1.update("DELETE FROM top_up_requests");
        jdbcTemplateDs0.update("DELETE FROM withdrawal_requests");
        jdbcTemplateDs1.update("DELETE FROM withdrawal_requests");
        jdbcTemplateDs0.update("DELETE FROM transfer_requests");
        jdbcTemplateDs1.update("DELETE FROM transfer_requests");
        jdbcTemplateDs0.update("DELETE FROM payment_requests");
        jdbcTemplateDs1.update("DELETE FROM payment_requests");
        jdbcTemplateDs0.update("DELETE FROM wallets");
        jdbcTemplateDs1.update("DELETE FROM wallets");
        jdbcTemplateDs0.update("DELETE FROM wallet_types");
        jdbcTemplateDs1.update("DELETE FROM wallet_types");
    }

    @Test
    @DisplayName("Создается wallet_type, wallet и делается запрос на создание TOP_UP payment_request")
    public void createPaymentRequestShouldReturnPaymentRequest(){
        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ownerWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseWallet = restTemplate.postForEntity(walletUrl, ownerWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdWallet = responseWallet.getBody();

        paymentRequestInputDto.setOwnerWalletUid(createdWallet.getUuid());
        paymentRequestInputDto.setType(RequestType.TOP_UP);

        ResponseEntity<PaymentRequestOutputDto> paymentRequestResponseEntity = restTemplate.postForEntity(requestUrl + "/request", paymentRequestInputDto, PaymentRequestOutputDto.class);
        PaymentRequestOutputDto createdPaymentRequest = paymentRequestResponseEntity.getBody();

        assertThat(paymentRequestResponseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(paymentRequestResponseEntity).isNotNull();
        assertThat(createdPaymentRequest.getId()).isNotNull();
        assertThat(createdPaymentRequest.getCreatedAt()).isNotNull();
        assertThat(createdPaymentRequest.getExpiredAt()).isNotNull();
        assertThat(createdPaymentRequest.getProfileUid()).isEqualTo(paymentRequestInputDto.getProfileUid());
        assertThat(createdPaymentRequest.getOwnerWalletUid()).isEqualTo(createdWallet.getUuid());
        assertThat(createdPaymentRequest.getAmount()).isEqualTo(paymentRequestInputDto.getAmount());
        assertThat(createdPaymentRequest.getFee()).isNotNull();
        assertThat(createdPaymentRequest.getType()).isEqualTo(paymentRequestInputDto.getType());
        assertThat(createdPaymentRequest.getStatus()).isEqualTo(Status.NEW);

        //проверка что top_up_request создался только на одной шарде, т. к. хеш юзер uid четный
        String sqlQuery = "SELECT COUNT(*) FROM top_up_requests WHERE payment_request_uid = ?";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdPaymentRequest.getId()}, Integer.class);
        assertThat(countInDs0).isEqualTo(1);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdPaymentRequest.getId()}, Integer.class);
        assertThat(countInDs1).isEqualTo(0);
    }

    @Test
    @DisplayName("Кошелек не существует, payment_request не создается")
    public void createTopUpPaymentRequestShouldThrowException(){
        paymentRequestInputDto.setOwnerWalletUid(UUID.randomUUID());
        paymentRequestInputDto.setType(RequestType.TOP_UP);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(requestUrl + "/request", paymentRequestInputDto, ErrorResponse.class);
        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(response.getBody().getMessage()).isEqualTo("Transaction failed");

        //проверка, что payment_request нигде не создан
        String sqlQuery = "SELECT COUNT(*) FROM payment_requests";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, Integer.class);
        assertThat(countInDs0).isEqualTo(0);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, Integer.class);
        assertThat(countInDs1).isEqualTo(0);
    }

    @Test
    @DisplayName("Создается wallet_type, 2 wallet, payment_request и делается запрос на создание TRANSFER транзакции")
    public void createTransactionShouldReturnTransaction(){
        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ownerWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseOwnerWallet = restTemplate.postForEntity(walletUrl, ownerWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdOwnerWallet = responseOwnerWallet.getBody();
        paymentRequestInputDto.setOwnerWalletUid(createdOwnerWallet.getUuid());
        recipientWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseRecipientWallet = restTemplate.postForEntity(walletUrl, recipientWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdRecipientWallet = responseRecipientWallet.getBody();
        paymentRequestInputDto.setWalletUidTo(createdRecipientWallet.getUuid());
        paymentRequestInputDto.setRecipientUid(recipientWalletInputDto.getProfileUid());
        paymentRequestInputDto.setType(RequestType.TRANSFER);
        ResponseEntity<PaymentRequestOutputDto> paymentRequestResponseEntity = restTemplate.postForEntity(requestUrl + "/request", paymentRequestInputDto, PaymentRequestOutputDto.class);
        PaymentRequestOutputDto createdPaymentRequest = paymentRequestResponseEntity.getBody();
        ConfirmRequestInputDto confirmRequestInputDto = new ConfirmRequestInputDto(createdPaymentRequest.getId(), paymentRequestInputDto.getProfileUid());
        ResponseEntity<TransactionOutputDto> transactionResponse = restTemplate.postForEntity(requestUrl + "/transaction", confirmRequestInputDto, TransactionOutputDto.class);
        assertThat(transactionResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(transactionResponse).isNotNull();

        TransactionOutputDto transaction = transactionResponse.getBody();
        assertThat(transaction.getUuid()).isNotNull();
        assertThat(transaction.getCreatedAt()).isNotNull();
        assertThat(transaction.getModifiedAt()).isNotNull();
        assertThat(transaction.getLinkedTransaction()).isNotNull();
        assertThat(transaction.getProfileUid()).isEqualTo(paymentRequestInputDto.getProfileUid());
        assertThat(transaction.getWalletUid()).isEqualTo(paymentRequestInputDto.getOwnerWalletUid());
        assertThat(transaction.getBalanceOperationAmount()).isNotNull();
        assertThat(transaction.getRawAmount()).isEqualTo(paymentRequestInputDto.getAmount());
        assertThat(transaction.getFee()).isNotNull();
        assertThat(transaction.getType()).isNotNull();
        assertThat(transaction.getPaymentRequestUid()).isEqualTo(createdPaymentRequest.getId());
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CREATED);

        //проверка что на каждой из шард создалось по одной транзакции, т. к. один хеш юзер uid четный, а второй - нечетный
        String sqlQuery = "SELECT COUNT(*) FROM transactions WHERE payment_request_uid = ?";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdPaymentRequest.getId()}, Integer.class);
        assertThat(countInDs0).isEqualTo(1);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdPaymentRequest.getId()}, Integer.class);
        assertThat(countInDs1).isEqualTo(1);
    }

    @Test
    @DisplayName("Создается wallet_type, 2 wallet, payment_request и делается запрос на создание TRANSFER транзакции, " +
            "но во время второго вызова репозитория выбрасывается исключение и транзакция откатывается")
    public void createTransactionShouldThrowExceptionAndRollbackTransaction(){
        Mockito.doCallRealMethod()
                .doThrow(new RuntimeException("Simulated exception on second call"))
                .when(transactionRepository)
                .create(Mockito.any(), Mockito.any());

        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ownerWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseOwnerWallet = restTemplate.postForEntity(walletUrl, ownerWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdOwnerWallet = responseOwnerWallet.getBody();
        paymentRequestInputDto.setOwnerWalletUid(createdOwnerWallet.getUuid());
        recipientWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseRecipientWallet = restTemplate.postForEntity(walletUrl, recipientWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdRecipientWallet = responseRecipientWallet.getBody();
        paymentRequestInputDto.setWalletUidTo(createdRecipientWallet.getUuid());
        paymentRequestInputDto.setRecipientUid(recipientWalletInputDto.getProfileUid());
        paymentRequestInputDto.setType(RequestType.TRANSFER);
        ResponseEntity<PaymentRequestOutputDto> paymentRequestResponseEntity = restTemplate.postForEntity(requestUrl + "/request", paymentRequestInputDto, PaymentRequestOutputDto.class);
        PaymentRequestOutputDto createdPaymentRequest = paymentRequestResponseEntity.getBody();
        ConfirmRequestInputDto confirmRequestInputDto = new ConfirmRequestInputDto(createdPaymentRequest.getId(), paymentRequestInputDto.getProfileUid());

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity(requestUrl + "/transaction", confirmRequestInputDto, ErrorResponse.class);
        assertThat(errorResponse.getStatusCode().is5xxServerError()).isTrue();
        assertThat(errorResponse.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("Transaction failed");

        //проверка что ни на одной из шард транзакции не создались
        String sqlQuery = "SELECT COUNT(*) FROM transactions WHERE payment_request_uid = ?";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdPaymentRequest.getId()}, Integer.class);
        assertThat(countInDs0).isEqualTo(0);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdPaymentRequest.getId()}, Integer.class);
        assertThat(countInDs1).isEqualTo(0);
    }

    @Test
    @DisplayName("Создается wallet_type, 2 wallet, payment_request, 2 transaction и делается запрос на проведение транзакции")
    public void confirmTransactionShouldReturnTransaction(){
        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ownerWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseOwnerWallet = restTemplate.postForEntity(walletUrl, ownerWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdOwnerWallet = responseOwnerWallet.getBody();
        paymentRequestInputDto.setOwnerWalletUid(createdOwnerWallet.getUuid());
        recipientWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseRecipientWallet = restTemplate.postForEntity(walletUrl, recipientWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdRecipientWallet = responseRecipientWallet.getBody();
        paymentRequestInputDto.setWalletUidTo(createdRecipientWallet.getUuid());
        paymentRequestInputDto.setRecipientUid(recipientWalletInputDto.getProfileUid());
        paymentRequestInputDto.setType(RequestType.TRANSFER);
        ResponseEntity<PaymentRequestOutputDto> paymentRequestResponseEntity = restTemplate.postForEntity(requestUrl + "/request", paymentRequestInputDto, PaymentRequestOutputDto.class);
        PaymentRequestOutputDto createdPaymentRequest = paymentRequestResponseEntity.getBody();
        ConfirmRequestInputDto confirmRequestInputDto = new ConfirmRequestInputDto(createdPaymentRequest.getId(), paymentRequestInputDto.getProfileUid());
        ResponseEntity<TransactionOutputDto> createTransactionResponse = restTemplate.postForEntity(requestUrl + "/transaction", confirmRequestInputDto, TransactionOutputDto.class);
        TransactionOutputDto createdTransaction = createTransactionResponse.getBody();

        ConfirmTransactionInputDto confirmTransactionDto = new ConfirmTransactionInputDto(createdTransaction.getUuid(), createdTransaction.getProfileUid());
        ResponseEntity<TransactionOutputDto> confirmTransactionResponse = restTemplate.postForEntity(requestUrl + "/confirm", confirmTransactionDto, TransactionOutputDto.class);

        assertThat(confirmTransactionResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(confirmTransactionResponse).isNotNull();

        TransactionOutputDto transaction = confirmTransactionResponse.getBody();
        assertThat(transaction.getUuid()).isNotNull();
        assertThat(transaction.getCreatedAt()).isNotNull();
        assertThat(transaction.getModifiedAt()).isNotNull();
        assertThat(transaction.getLinkedTransaction()).isNotNull();
        assertThat(transaction.getProfileUid()).isEqualTo(paymentRequestInputDto.getProfileUid());
        assertThat(transaction.getWalletUid()).isEqualTo(paymentRequestInputDto.getOwnerWalletUid());
        assertThat(transaction.getBalanceOperationAmount()).isNotNull();
        assertThat(transaction.getRawAmount()).isEqualTo(paymentRequestInputDto.getAmount());
        assertThat(transaction.getFee()).isNotNull();
        assertThat(transaction.getType()).isNotNull();
        assertThat(transaction.getPaymentRequestUid()).isEqualTo(createdPaymentRequest.getId());
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);


        //проверка балансов кошельков на разных шардах, т. к. один хеш юзер uid четный, а второй - нечетный
        String sqlQuery = "SELECT balance FROM wallets WHERE uuid = ?";
        BigDecimal balanceInOwnerWallet = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdOwnerWallet.getUuid()}, BigDecimal.class);
        BigDecimal expectedOwnerBalance = ownerWalletInputDto.getBalance().subtract(paymentRequestInputDto.getAmount());
        assertThat(balanceInOwnerWallet).isLessThan(expectedOwnerBalance);

        BigDecimal balanceInRecipientWallet = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdRecipientWallet.getUuid()}, BigDecimal.class);
        BigDecimal expectedRecipientBalance = recipientWalletInputDto.getBalance().add(paymentRequestInputDto.getAmount());
        assertThat(balanceInRecipientWallet).isEqualTo(expectedRecipientBalance);
    }

    @Test
    @DisplayName("Создается wallet_type, 2 wallet, payment_request, 2 transaction и делается запрос на проведение транзакции, " +
            "но во время обновления баланса второго кошелька выбрасывается исключение и транзакция откатывается")
    public void confirmTransactionShouldThrowExceptionAndRollbackTransaction(){
        Mockito.doCallRealMethod()
                .doThrow(new RuntimeException("Simulated exception on second call"))
                .when(walletRepository)
                .updateBalance(Mockito.any(), Mockito.any(), Mockito.any());

        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ownerWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseOwnerWallet = restTemplate.postForEntity(walletUrl, ownerWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdOwnerWallet = responseOwnerWallet.getBody();
        paymentRequestInputDto.setOwnerWalletUid(createdOwnerWallet.getUuid());
        recipientWalletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseRecipientWallet = restTemplate.postForEntity(walletUrl, recipientWalletInputDto, WalletOutputDto.class);
        WalletOutputDto createdRecipientWallet = responseRecipientWallet.getBody();
        paymentRequestInputDto.setWalletUidTo(createdRecipientWallet.getUuid());
        paymentRequestInputDto.setRecipientUid(recipientWalletInputDto.getProfileUid());
        paymentRequestInputDto.setType(RequestType.TRANSFER);
        ResponseEntity<PaymentRequestOutputDto> paymentRequestResponseEntity = restTemplate.postForEntity(requestUrl + "/request", paymentRequestInputDto, PaymentRequestOutputDto.class);
        PaymentRequestOutputDto createdPaymentRequest = paymentRequestResponseEntity.getBody();
        ConfirmRequestInputDto confirmRequestInputDto = new ConfirmRequestInputDto(createdPaymentRequest.getId(), paymentRequestInputDto.getProfileUid());
        ResponseEntity<TransactionOutputDto> createTransactionResponse = restTemplate.postForEntity(requestUrl + "/transaction", confirmRequestInputDto, TransactionOutputDto.class);
        TransactionOutputDto createdTransaction = createTransactionResponse.getBody();

        ConfirmTransactionInputDto confirmTransactionDto = new ConfirmTransactionInputDto(createdTransaction.getUuid(), createdTransaction.getProfileUid());
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity(requestUrl + "/confirm", confirmTransactionDto, ErrorResponse.class);
        assertThat(errorResponse.getStatusCode().is5xxServerError()).isTrue();
        assertThat(errorResponse.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("Transaction failed");


        //проверка балансов кошельков на разных шардах, т. к. один хеш юзер uid четный, а второй - нечетный
        String sqlQuery = "SELECT balance FROM wallets WHERE uuid = ?";
        BigDecimal balanceInOwnerWallet = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdOwnerWallet.getUuid()}, BigDecimal.class);
        assertThat(balanceInOwnerWallet).isEqualTo(ownerWalletInputDto.getBalance());

        BigDecimal balanceInRecipientWallet = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdRecipientWallet.getUuid()}, BigDecimal.class);
        assertThat(balanceInRecipientWallet).isEqualTo(recipientWalletInputDto.getBalance());
    }
}
