package com.milko.wallet_service.testcontainers;

import com.milko.wallet_service.config.JdbcTemplateTestConfig;
import com.milko.wallet_service.dto.Status;
import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.exceptionhandling.ErrorResponse;
import com.milko.wallet_service.repository.WalletTypeRepository;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JdbcTemplateTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WalletTypeControllerTest {

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
    private WalletTypeRepository walletTypeRepository;

    WalletTypeInputDto inputDto;

    ChangeWalletTypeInputDto changeWalletTypeDto;
    private String URL;

    @BeforeEach
    public void init(){
        inputDto = WalletTypeInputDto.builder()
                .name("name")
                .currencyCode("UAH")
                .status(Status.ACTIVE)
                .profileType("type")
                .creator("creator")
                .build();
        changeWalletTypeDto = ChangeWalletTypeInputDto.builder()
                .changedByUserUid(UUID.randomUUID())
                .changedByProfileType("type")
                .reason("reason")
                .comment("comment")
                .toStatus(Status.NEW)
                .build();
        URL = "http://localhost:" + port + "/api/v1/wallet_types";
    }

    @AfterEach
    public void cleanDatabase(){
        jdbcTemplateDs0.update("DELETE FROM wallet_types_status_history");
        jdbcTemplateDs1.update("DELETE FROM wallet_types_status_history");
        jdbcTemplateDs0.update("DELETE FROM wallet_types");
        jdbcTemplateDs1.update("DELETE FROM wallet_types");
    }


    @Test
    void testCreateWalletTypeShouldCreateWalletType() {
        ResponseEntity<UUID> response = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(createdWalletTypeId).isNotNull();

        String sqlQuery = "SELECT COUNT(*) FROM wallet_types WHERE uuid = ?";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, Integer.class);
        assertThat(countInDs0).isEqualTo(1);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, Integer.class);
        assertThat(countInDs1).isEqualTo(1);
    }

    @Test
    void testCreateWalletTypeShouldThrowTransactionFailed() {
        Mockito.doCallRealMethod()
                .doThrow(new RuntimeException("Simulated exception on second call"))
                .when(walletTypeRepository)
                .create(Mockito.any(), Mockito.any());

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(URL, inputDto, ErrorResponse.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(response.getBody().getMessage()).isEqualTo("Transaction failed");

        String sqlQuery = "SELECT COUNT(*) FROM wallet_types";
        Integer countInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, Integer.class);
        assertThat(countInDs0).isEqualTo(0);

        Integer countInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, Integer.class);
        assertThat(countInDs1).isEqualTo(0);
    }

    @Test
    void testFindByIdWalletTypeShouldReturnWalletType() {
        ResponseEntity<UUID> response = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ResponseEntity<WalletTypeOutputDto> walletTypeResponse = restTemplate.getForEntity(URL + "/" + createdWalletTypeId, WalletTypeOutputDto.class);
        assertThat(walletTypeResponse.getStatusCode().is2xxSuccessful()).isTrue();
        WalletTypeOutputDto walletType = walletTypeResponse.getBody();
        assertThat(walletType).isNotNull();
        assertThat(walletType.getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(walletType.getName()).isEqualTo(inputDto.getName());
        assertThat(walletType.getCurrencyCode()).isEqualTo(inputDto.getCurrencyCode());
        assertThat(walletType.getStatus()).isEqualTo(inputDto.getStatus());
        assertThat(walletType.getProfileType()).isEqualTo(inputDto.getProfileType());
        assertThat(walletType.getCreator()).isEqualTo(inputDto.getCreator());
        assertThat(walletType.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByIdWalletTypeShouldThrowNotFoundException() {
        UUID randomId = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(URL + "/" + randomId, ErrorResponse.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("NotFoundException");
        assertThat(response.getBody().getMessage()).isEqualTo("WalletType with id " + randomId + " not found");
    }

    @Test
    void testFindAllWalletTypesShouldReturnListOfOneWalletType() {
        ResponseEntity<UUID> response = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ResponseEntity<WalletTypeOutputDto[]> walletTypesResponse = restTemplate.getForEntity(URL, WalletTypeOutputDto[].class);

        assertThat(walletTypesResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(walletTypesResponse.getBody()).hasSize(1);

        WalletTypeOutputDto walletType = walletTypesResponse.getBody()[0];
        assertThat(walletType.getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(walletType.getName()).isEqualTo(inputDto.getName());
        assertThat(walletType.getCurrencyCode()).isEqualTo(inputDto.getCurrencyCode());
        assertThat(walletType.getStatus()).isEqualTo(inputDto.getStatus());
        assertThat(walletType.getProfileType()).isEqualTo(inputDto.getProfileType());
        assertThat(walletType.getCreator()).isEqualTo(inputDto.getCreator());
        assertThat(walletType.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindAllWalletTypesShouldReturnEmptyList() {
        ResponseEntity<WalletTypeOutputDto[]> walletTypesResponse = restTemplate.getForEntity(URL, WalletTypeOutputDto[].class);

        assertThat(walletTypesResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(walletTypesResponse.getBody()).isEmpty();
    }

    //проверяю что при обновлении wallet_type на обеих шардах создается история обновления
    @Test
    void testUpdateWalletTypeShouldUpdateStatus() {
        ResponseEntity<UUID> createdWalletId = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = createdWalletId.getBody();
        changeWalletTypeDto.setWalletTypeId(createdWalletTypeId);

        RestTemplate patchRestTemplate = getPatchRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<ChangeWalletTypeInputDto> requestEntity = new HttpEntity<>(changeWalletTypeDto, headers);
        ResponseEntity<WalletTypeOutputDto> response = patchRestTemplate.exchange(
                URL,
                HttpMethod.PATCH,
                requestEntity,
                WalletTypeOutputDto.class
        );
        WalletTypeOutputDto walletType = response.getBody();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(walletType.getUuid()).isEqualTo(createdWalletTypeId);
        assertThat(walletType.getName()).isEqualTo(inputDto.getName());
        assertThat(walletType.getCurrencyCode()).isEqualTo(inputDto.getCurrencyCode());
        assertThat(walletType.getStatus()).isEqualTo(changeWalletTypeDto.getToStatus());
        assertThat(walletType.getProfileType()).isEqualTo(inputDto.getProfileType());
        assertThat(walletType.getCreator()).isEqualTo(inputDto.getCreator());
        assertThat(walletType.getCreatedAt()).isNotNull();
        assertThat(walletType.getModifiedAt()).isNotNull();

        String sqlQuery = "SELECT count(*) FROM wallet_types_status_history WHERE wallet_type_id = ?";
        Integer historyCountInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, Integer.class);
        assertThat(historyCountInDs0).isEqualTo(1);

        Integer historyCountInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, Integer.class);
        assertThat(historyCountInDs1).isEqualTo(1);
    }

    @Test
    void testUpdateWalletTypeShouldThrowNotFound() {
        UUID randomId = UUID.randomUUID();
        changeWalletTypeDto.setWalletTypeId(randomId);

        RestTemplate patchRestTemplate = getPatchRestTemplate();

        HttpEntity<ChangeWalletTypeInputDto> requestEntity = new HttpEntity<>(changeWalletTypeDto, new HttpHeaders());
        ResponseEntity<ErrorResponse> response = patchRestTemplate.exchange(
                URL,
                HttpMethod.PATCH,
                requestEntity,
                ErrorResponse.class
        );
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody().getType()).isEqualTo("NotFoundException");
        assertThat(response.getBody().getMessage()).isEqualTo("WalletType with id " + randomId + " not found");
    }

    @Test
    void testUpdateWalletTypeShouldThrowTransactionFailed() {
        Mockito.doCallRealMethod()
                .doThrow(new RuntimeException("Simulated exception on second call"))
                .when(walletTypeRepository)
                .updateStatus(Mockito.any(), Mockito.any(), Mockito.any());

        ResponseEntity<UUID> response = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();
        changeWalletTypeDto.setWalletTypeId(createdWalletTypeId);

        RestTemplate patchRestTemplate = getPatchRestTemplate();

        HttpEntity<ChangeWalletTypeInputDto> requestEntity = new HttpEntity<>(changeWalletTypeDto, new HttpHeaders());
        ResponseEntity<ErrorResponse> errorResponse = patchRestTemplate.exchange(
                URL,
                HttpMethod.PATCH,
                requestEntity,
                ErrorResponse.class
        );
        assertThat(errorResponse.getStatusCode().is5xxServerError()).isTrue();
        assertThat(errorResponse.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("Transaction failed");

        String sqlQuery = "SELECT status FROM wallet_types WHERE uuid = ?";
        String statusInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, String.class);
        assertThat(statusInDs0).isEqualTo(Status.ACTIVE.toString());

        String statusInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, String.class);
        assertThat(statusInDs1).isEqualTo(Status.ACTIVE.toString());

        String sqlQueryForHistoryCheck = "SELECT count(*) FROM wallet_types_status_history WHERE wallet_type_id = ?";
        Integer historyCountInDs0 = jdbcTemplateDs0.queryForObject(sqlQueryForHistoryCheck, new Object[]{createdWalletTypeId}, Integer.class);
        assertThat(historyCountInDs0).isEqualTo(0);

        Integer historyCountInDs1 = jdbcTemplateDs1.queryForObject(sqlQueryForHistoryCheck, new Object[]{createdWalletTypeId}, Integer.class);
        assertThat(historyCountInDs1).isEqualTo(0);
    }

    @Test
    void testDeleteWalletTypeShouldChangeStatusToDelete() {
        ResponseEntity<UUID> response = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ResponseEntity<Boolean> deleteResponse = restTemplate.exchange(
                URL + "/" + createdWalletTypeId,
                HttpMethod.DELETE,
                null,
                Boolean.class
        );

        assertThat(deleteResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(deleteResponse.getBody()).isTrue();

        String sqlQuery = "SELECT status FROM wallet_types WHERE uuid = ?";
        String statusInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, String.class);
        assertThat(statusInDs0).isEqualTo(Status.DELETED.toString());

        String statusInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, String.class);
        assertThat(statusInDs1).isEqualTo(Status.DELETED.toString());
    }

    @Test
    void testDeleteWalletTypeShouldThrowTransactionFailed() {
        Mockito.doCallRealMethod()
                .doThrow(new RuntimeException("Simulated exception on second call"))
                .when(walletTypeRepository)
                .deleteById(Mockito.any(), Mockito.any());

        ResponseEntity<UUID> response = restTemplate.postForEntity(URL, inputDto, UUID.class);
        UUID createdWalletTypeId = response.getBody();

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(
                URL + "/" + createdWalletTypeId,
                HttpMethod.DELETE,
                null,
                ErrorResponse.class
        );

        assertThat(errorResponse.getStatusCode().is5xxServerError()).isTrue();
        assertThat(errorResponse.getBody().getType()).isEqualTo("TransactionFailedException");
        assertThat(errorResponse.getBody().getMessage()).isEqualTo("Transaction failed");

        String sqlQuery = "SELECT status FROM wallet_types WHERE uuid = ?";
        String statusInDs0 = jdbcTemplateDs0.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, String.class);
        assertThat(statusInDs0).isEqualTo(Status.ACTIVE.toString());

        String statusInDs1 = jdbcTemplateDs1.queryForObject(sqlQuery, new Object[]{createdWalletTypeId}, String.class);
        assertThat(statusInDs1).isEqualTo(Status.ACTIVE.toString());
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
