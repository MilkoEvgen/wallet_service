package com.milko.wallet_service.testcontainers;

import com.milko.wallet_service.config.JdbcTemplateTestConfig;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.input.WalletRequestInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.exceptionhandling.ErrorResponse;
import com.milko.wallet_service.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Import(JdbcTemplateTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WalletControllerTest {
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
    private WalletRepository walletRepository;

    WalletTypeInputDto walletTypeInputDto;
    WalletInputDto walletInputDto;
    ChangeWalletInputDto changeWalletDto;
    private String walletTypeUrl;
    private String walletUrl;

    @BeforeEach
    public void init(){
        walletTypeInputDto = WalletTypeInputDto.builder()
                .name("name")
                .currencyCode("UAH")
                .status(Status.ACTIVE)
                .profileType("type")
                .creator("creator")
                .build();
        walletInputDto = WalletInputDto.builder()
                .name("wallet")
                .profileUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50239"))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100))
                .build();
        changeWalletDto = ChangeWalletInputDto.builder()
                .changedByUserUid(UUID.fromString("55749ef3-83cb-4855-beb0-41a135e50239"))
                .changedByProfileType("type")
                .reason("reason")
                .comment("comment")
                .toStatus(Status.NEW)
                .build();
        walletTypeUrl = "http://localhost:" + port + "/api/v1/wallet_types";
        walletUrl = "http://localhost:" + port + "/api/v1/wallets";
    }

    @AfterEach
    public void cleanDatabase(){
        jdbcTemplateDs0.update("DELETE FROM wallet_status_history");
        jdbcTemplateDs1.update("DELETE FROM wallet_status_history");
        jdbcTemplateDs0.update("DELETE FROM wallets");
        jdbcTemplateDs1.update("DELETE FROM wallets");
        jdbcTemplateDs0.update("DELETE FROM wallet_types");
        jdbcTemplateDs1.update("DELETE FROM wallet_types");
    }

    @Test
    public void createShouldReturnWallet(){
        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        walletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> responseWallet = restTemplate.postForEntity(walletUrl, walletInputDto, WalletOutputDto.class);
        WalletOutputDto createdWallet = responseWallet.getBody();

        assertThat(responseWallet.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseWallet).isNotNull();
        assertThat(createdWallet.getUuid()).isNotNull();
        assertThat(createdWallet.getCreatedAt()).isNotNull();
        assertThat(createdWallet.getName()).isEqualTo(walletInputDto.getName());
        assertThat(createdWallet.getWalletType().getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(createdWallet.getProfileUid()).isEqualTo(walletInputDto.getProfileUid());
        assertThat(createdWallet.getStatus()).isEqualTo(walletInputDto.getStatus());
        assertThat(createdWallet.getBalance()).isEqualTo(walletInputDto.getBalance());

        String sqlQuery = "SELECT COUNT(*) FROM wallets WHERE uuid = ?";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdWallet.getUuid()}, Integer.class);
        assertThat(countInDs0).isEqualTo(1);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdWallet.getUuid()}, Integer.class);
        assertThat(countInDs1).isEqualTo(0);
    }

    @Test
    public void createShouldThrowNotFoundExceptionIfWalletTypeNotExists(){
        UUID randomId = UUID.randomUUID();
        walletInputDto.setWalletTypeId(randomId);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(walletUrl, walletInputDto, ErrorResponse.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("NotFoundException");
        assertThat(response.getBody().getMessage()).isEqualTo("WalletType with id " + randomId + " not found");
    }

    @Test
    public void findByIdShouldReturnWallet(){
        ResponseEntity<UUID> response = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        walletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> createdWallet = restTemplate.postForEntity(walletUrl, walletInputDto, WalletOutputDto.class);
        UUID createdWalletId = createdWallet.getBody().getUuid();

        WalletRequestInputDto walletRequest = new WalletRequestInputDto(createdWalletId, walletInputDto.getProfileUid());

        ResponseEntity<WalletOutputDto> responseWallet = restTemplate.postForEntity(walletUrl + "/get", walletRequest, WalletOutputDto.class);
        WalletOutputDto wallet = responseWallet.getBody();

        assertThat(responseWallet.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseWallet).isNotNull();
        assertThat(wallet.getUuid()).isNotNull();
        assertThat(wallet.getCreatedAt()).isNotNull();
        assertThat(wallet.getName()).isEqualTo(walletInputDto.getName());
        assertThat(wallet.getWalletType().getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(wallet.getProfileUid()).isEqualTo(walletInputDto.getProfileUid());
        assertThat(wallet.getStatus()).isEqualTo(walletInputDto.getStatus());
        assertThat(wallet.getBalance()).isEqualTo(walletInputDto.getBalance());
    }

    @Test
    public void findByIdShouldThrowNotFoundExceptionIfWalletNotExists(){
        UUID randomId = UUID.randomUUID();
        WalletRequestInputDto walletRequest = new WalletRequestInputDto(randomId, walletInputDto.getProfileUid());

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(walletUrl + "/get", walletRequest, ErrorResponse.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("NotFoundException");
        assertThat(response.getBody().getMessage()).isEqualTo("Wallet with id " + randomId + " not found");
    }

    @Test
    public void findAllByProfileIdShouldReturnListOfWallets(){
        ResponseEntity<UUID> walletTypeResponse = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = walletTypeResponse.getBody();

        walletInputDto.setWalletTypeId(createdWalletTypeId);

        restTemplate.postForEntity(walletUrl, walletInputDto, WalletOutputDto.class);

        ResponseEntity<WalletOutputDto[]> response = restTemplate.getForEntity(walletUrl + "/" + walletInputDto.getProfileUid(),WalletOutputDto[].class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);

        WalletOutputDto wallet = response.getBody()[0];

        assertThat(wallet.getUuid()).isNotNull();
        assertThat(wallet.getCreatedAt()).isNotNull();
        assertThat(wallet.getName()).isEqualTo(walletInputDto.getName());
        assertThat(wallet.getWalletType().getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(wallet.getProfileUid()).isEqualTo(walletInputDto.getProfileUid());
        assertThat(wallet.getStatus()).isEqualTo(walletInputDto.getStatus());
        assertThat(wallet.getBalance()).isEqualTo(walletInputDto.getBalance());
    }

    @Test
    public void findAllByProfileIdShouldReturnEmptyList(){
        ResponseEntity<WalletOutputDto[]> response = restTemplate.getForEntity(walletUrl + "/" + walletInputDto.getProfileUid(),WalletOutputDto[].class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(0);
    }

    //проверяю что кошелек обновляется и на одной из шард создается история
    //шарда относится к юзеру с четным хешкодом uid
    @Test
    public void updateShouldReturnUpdatedWallet(){
        ResponseEntity<UUID> walletTypeResponse = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = walletTypeResponse.getBody();

        walletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> createdWallet = restTemplate.postForEntity(walletUrl, walletInputDto, WalletOutputDto.class);
        UUID createdWalletId = createdWallet.getBody().getUuid();

        RestTemplate patchRestTemplate = getPatchRestTemplate();

        changeWalletDto.setWalletId(createdWalletId);
        HttpEntity<ChangeWalletInputDto> requestEntity = new HttpEntity<>(changeWalletDto, new HttpHeaders());
        ResponseEntity<WalletOutputDto> walletOutputDtoResponse = patchRestTemplate.exchange(
                walletUrl,
                HttpMethod.PATCH,
                requestEntity,
                WalletOutputDto.class
        );
        assertThat(walletOutputDtoResponse.getStatusCode().is2xxSuccessful()).isTrue();

        WalletOutputDto wallet = walletOutputDtoResponse.getBody();
        assertThat(wallet.getUuid()).isNotNull();
        assertThat(wallet.getCreatedAt()).isNotNull();
        assertThat(wallet.getName()).isEqualTo(walletInputDto.getName());
        assertThat(wallet.getWalletType().getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(wallet.getProfileUid()).isEqualTo(walletInputDto.getProfileUid());
        assertThat(wallet.getStatus()).isEqualTo(Status.NEW);
        assertThat(wallet.getBalance()).isEqualTo(walletInputDto.getBalance());

        String sqlQueryForHistoryCheck = "SELECT count(*) FROM wallet_status_history WHERE wallet_uid = ?";
        Integer historyCountInDs0 = jdbcTemplateDs0.queryForObject(sqlQueryForHistoryCheck, new Object[]{createdWalletId}, Integer.class);
        assertThat(historyCountInDs0).isEqualTo(1);

        Integer historyCountInDs1 = jdbcTemplateDs1.queryForObject(sqlQueryForHistoryCheck, new Object[]{createdWalletId}, Integer.class);
        assertThat(historyCountInDs1).isEqualTo(0);
    }

    @Test
    public void updateShouldThrowException(){
        UUID randomId = UUID.randomUUID();
        RestTemplate patchRestTemplate = getPatchRestTemplate();

        changeWalletDto.setWalletId(randomId);
        HttpEntity<ChangeWalletInputDto> requestEntity = new HttpEntity<>(changeWalletDto, new HttpHeaders());
        ResponseEntity<ErrorResponse> response = patchRestTemplate.exchange(
                walletUrl,
                HttpMethod.PATCH,
                requestEntity,
                ErrorResponse.class
        );
        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(response.getBody().getMessage()).isEqualTo("Transaction failed");
    }

    @Test
    public void deleteShouldReturnDeletedWallet(){
        ResponseEntity<UUID> walletTypeResponse = restTemplate.postForEntity(walletTypeUrl, walletTypeInputDto, UUID.class);
        UUID createdWalletTypeId = walletTypeResponse.getBody();

        walletInputDto.setWalletTypeId(createdWalletTypeId);

        ResponseEntity<WalletOutputDto> createdWallet = restTemplate.postForEntity(walletUrl, walletInputDto, WalletOutputDto.class);
        UUID createdWalletId = createdWallet.getBody().getUuid();

        WalletRequestInputDto walletRequestDto = new WalletRequestInputDto(createdWalletId, walletInputDto.getProfileUid());

        ResponseEntity<Boolean> deletedWalletResponse = restTemplate.postForEntity(walletUrl + "/delete", walletRequestDto, Boolean.class);
        assertThat(deletedWalletResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(deletedWalletResponse.getBody()).isTrue();
    }

    @Test
    public void deleteShouldShouldThrowException(){
        UUID randomId = UUID.randomUUID();

        WalletRequestInputDto walletRequestDto = new WalletRequestInputDto(randomId, walletInputDto.getProfileUid());

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(walletUrl + "/delete", walletRequestDto, ErrorResponse.class);
        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(response.getBody().getMessage()).isEqualTo("Transaction failed");
    }

    private RestTemplate getPatchRestTemplate(){
        RestTemplate patchRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));

        patchRestTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
        return patchRestTemplate;
    }




}
