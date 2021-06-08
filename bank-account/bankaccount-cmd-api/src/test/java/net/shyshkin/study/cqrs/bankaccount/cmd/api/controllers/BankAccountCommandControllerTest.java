package net.shyshkin.study.cqrs.bankaccount.cmd.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.DepositFundsCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.OpenAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.bankaccount.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.bankaccount.core.dto.OpenAccountResponse;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankAccountCommandControllerTest extends AbstractDockerComposeTest {

    static UUID existingAccountId;

    @BeforeEach
    void setUp() {
        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");

        restTemplate = new TestRestTemplate(restTemplateBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .rootUri("http://localhost:" + randomServerPort));
    }

    @Test
    @Order(10)
    void createAccountCorrect() {
        //given
        UUID id = UUID.randomUUID();
        var command = OpenAccountCommand.builder()
                .id(id)
                .accountHolderId(UUID.randomUUID().toString())
                .openingBalance(new BigDecimal("123.00"))
                .accountType(AccountType.CURRENT)
                .build();
        String expectedMessage = "Account created successfully";

        //when
        ResponseEntity<OpenAccountResponse> responseEntity = restTemplate
                .postForEntity("/api/v1/accounts", command, OpenAccountResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OpenAccountResponse openAccountResponse = responseEntity.getBody();
        assertThat(openAccountResponse)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
        ;
        UUID idReceived = openAccountResponse.getId();
        assertThat(idReceived).isNotEqualTo(id);
        URI location = responseEntity.getHeaders().getLocation();
        assertThat(location.toString()).contains("/api/v1/accounts/" + idReceived);
        existingAccountId = idReceived;
    }

    @Test
    @Order(20)
    void createAccount_nullId() {
        //given
        var command = OpenAccountCommand.builder()
                .accountHolderId(UUID.randomUUID().toString())
                .openingBalance(new BigDecimal("123.00"))
                .accountType(AccountType.CURRENT)
                .build();
        String expectedMessage = "Account created successfully";

        //when
        ResponseEntity<OpenAccountResponse> responseEntity = restTemplate
                .postForEntity("/api/v1/accounts", command, OpenAccountResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OpenAccountResponse openAccountResponse = responseEntity.getBody();
        assertThat(openAccountResponse)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
        ;

        UUID idReceived = openAccountResponse.getId();
        URI location = responseEntity.getHeaders().getLocation();
        assertThat(location.toString()).contains("/api/v1/accounts/" + idReceived);
        existingAccountId = idReceived;
    }

    @Test
    @Order(30)
    void createAccount_validationFailed() {
        //given
        var command = OpenAccountCommand.builder()
                .accountHolderId(UUID.randomUUID().toString())
                .openingBalance(new BigDecimal("-1.00"))
                .accountType(AccountType.CURRENT)
                .build();

        //when
        var responseEntity = restTemplate
                .postForEntity("/api/v1/accounts", command, BaseResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        BaseResponse baseResponse = responseEntity.getBody();
        assertThat(baseResponse.getMessage())
                .contains("Validation failed for argument")
                .contains("Opening balance must not be negative");
    }

    @Test
    @Order(40)
    void depositFunds_correct() {
        //given
        UUID id = existingAccountId;
        var command = DepositFundsCommand.builder()
                .id(id)
                .amount(new BigDecimal("300.00"))
                .build();
        String expectedMessage = "Funds deposited successfully";

        //when
        var requestEntity = RequestEntity
                .put("/api/v1/accounts/{id}/deposits", id)
                .body(command);

        var responseEntity = restTemplate
                .exchange(requestEntity, BaseResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
        ;
    }

}