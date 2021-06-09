package net.shyshkin.study.cqrs.bankaccount.query.api;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.query.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.bankaccount.query.api.dto.AccountLookupResponse;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.EqualityType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankAccountQueryApiApplicationTest extends AbstractDockerComposeTest {

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
    void findAllAccounts_empty() {

        //given
        String expectedMessage = "Bank Accounts not found";

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts", AccountLookupResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
        ;
    }

    @Test
    @Order(20)
    void findAccountById_absent() {

        //given
        UUID id = UUID.randomUUID();
        String expectedMessage = String.format("Bank Account with id `%s` not found", id);

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts/{id}", AccountLookupResponse.class, id);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("message", expectedMessage)
        ;
    }

    @Test
    @Order(30)
    void findAccountHolderId_absent() {

        //given
        UUID accountHolderId = UUID.randomUUID();
        String expectedMessage = String.format("Bank Account with holder id `%s` not found", accountHolderId);

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts?accountHolderId={holderId}", AccountLookupResponse.class, accountHolderId);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasFieldOrPropertyWithValue("message", expectedMessage)
        ;
    }

    @Test
    @Order(40)
    void findAccountsWithBalance_absent() {

        //given
        EqualityType equalityType = EqualityType.GREATER_THEN;
        BigDecimal balance = new BigDecimal("321.12");

        String expectedMessage = "Bank Accounts not found";

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts?equalityType={equalityType}&balance={balance}",
                        AccountLookupResponse.class, equalityType, balance);

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
