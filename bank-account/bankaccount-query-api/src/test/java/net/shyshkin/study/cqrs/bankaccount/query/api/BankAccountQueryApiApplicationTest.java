package net.shyshkin.study.cqrs.bankaccount.query.api;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.OpenAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;
import net.shyshkin.study.cqrs.bankaccount.query.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.bankaccount.query.api.dto.AccountLookupResponse;
import net.shyshkin.study.cqrs.bankaccount.query.api.queries.EqualityType;
import net.shyshkin.study.cqrs.bankaccount.query.api.repositories.AccountRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankAccountQueryApiApplicationTest extends AbstractDockerComposeTest {

    static UUID existingAccountId;
    static String existingHolderId;

    @Autowired
    AccountRepository accountRepository;

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

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts", AccountLookupResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getBody()).isNull();
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

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts?equalityType={equalityType}&balance={balance}",
                        AccountLookupResponse.class, equalityType, balance);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    @Order(50)
    void findAllAccounts_presentOne() {

        //given
        String expectedMessage = "Successfully returned 1 Bank Account(s)";
        createRandomBankAccount();

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
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(1)
                        .allSatisfy(bankAccount -> assertThat(bankAccount)
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrPropertyWithValue("id", existingAccountId)
                                .hasFieldOrPropertyWithValue("accountHolderId", existingHolderId)
                        )
                );
    }

    @Test
    @Order(55)
    void findAccountById_presentOne() {

        //given
        UUID id = existingAccountId;
        String expectedMessage = "Bank Account successfully returned";

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts/{id}", AccountLookupResponse.class, id);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(1)
                        .allSatisfy(bankAccount -> assertThat(bankAccount)
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrPropertyWithValue("id", existingAccountId)
                                .hasFieldOrPropertyWithValue("accountHolderId", existingHolderId)
                        )
                );
    }

    @Test
    @Order(60)
    void findAccountByHolderId_presentOne() {

        //given
        String holderId = existingHolderId;
        String expectedMessage = "Bank Accounts successfully returned";

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts?accountHolderId={holderId}", AccountLookupResponse.class, holderId);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(1)
                        .allSatisfy(bankAccount -> assertThat(bankAccount)
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrPropertyWithValue("id", existingAccountId)
                                .hasFieldOrPropertyWithValue("accountHolderId", existingHolderId)
                        )
                );
    }

    @Test
    @Order(65)
    void findAccountsWithBalance_presentOne() {

        //given
        EqualityType equalityType = EqualityType.GREATER_THEN;
        BigDecimal balance = new BigDecimal("100.12");

        String expectedMessage = "Successfully returned 1 Bank Account(s)";

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
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(1)
                        .allSatisfy(bankAccount -> assertThat(bankAccount)
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrPropertyWithValue("id", existingAccountId)
                                .hasFieldOrPropertyWithValue("accountHolderId", existingHolderId)
                        )
                );
    }

    @Test
    @Order(70)
    void findAllAccounts_presentFour() {

        //given
        String expectedMessage = "Successfully returned 4 Bank Account(s)";
        create3BankAccounts();

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
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(4)
                        .allSatisfy(bankAccount -> assertThat(bankAccount)
                                .hasNoNullFieldsOrProperties()
                        )
                );
    }

    @Test
    @Order(75)
    void findAccountByHolderId_presentTwo() {

        //given
        String holderId = existingHolderId;
        String expectedMessage = "Bank Accounts successfully returned";

        //when
        var responseEntity = restTemplate
                .getForEntity("/api/v1/accounts?accountHolderId={holderId}", AccountLookupResponse.class, holderId);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var response = responseEntity.getBody();
        assertThat(response)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("message", expectedMessage)
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(2)
                        .allSatisfy(bankAccount -> assertThat(bankAccount)
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrPropertyWithValue("accountHolderId", existingHolderId)
                        )
                );
    }

    @Test
    @Order(80)
    void findAccountsWithBalance_greaterThen_mustBe2() {

        //given
        EqualityType equalityType = EqualityType.GREATER_THEN;
        BigDecimal balance = new BigDecimal("200.00");

        String expectedMessage = "Successfully returned 2 Bank Account(s)";

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
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(2)
                        .allSatisfy(bankAccount -> assertThat(bankAccount).hasNoNullFieldsOrProperties())
                        .allSatisfy(bankAccount -> assertThat(bankAccount.getBalance()).isGreaterThan(balance))
                );
    }

    @Test
    @Order(85)
    void findAccountsWithBalance_lessThen_mustBe3() {

        //given
        EqualityType equalityType = EqualityType.LESS_THEN;
        BigDecimal balance = new BigDecimal("400.00");

        String expectedMessage = "Successfully returned 3 Bank Account(s)";

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
                .satisfies(resp -> assertThat(resp.getAccounts())
                        .hasSize(3)
                        .allSatisfy(bankAccount -> assertThat(bankAccount).hasNoNullFieldsOrProperties())
                        .allSatisfy(bankAccount -> assertThat(bankAccount.getBalance()).isLessThan(balance))
                );
    }


    private void createRandomBankAccount() {
        OpenAccountCommand openAccountCommand = OpenAccountCommand.builder()
                .id(UUID.randomUUID())
                .accountHolderId(UUID.randomUUID().toString())
                .accountType(AccountType.CURRENT)
                .openingBalance(new BigDecimal("321.12"))
                .build();

        commandGateway.sendAndWait(openAccountCommand);

        existingAccountId = openAccountCommand.getId();
        existingHolderId = openAccountCommand.getAccountHolderId();

        await()
                .timeout(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(accountRepository.findById(existingAccountId))
                        .hasValueSatisfying(bankAccount -> assertThat(bankAccount.getAccountHolderId())
                                .isEqualTo(existingHolderId)));
    }

    private void create3BankAccounts() {

        long initialCount = accountRepository.count();
        List<OpenAccountCommand> commands = List.of(
                OpenAccountCommand.builder()
                        .id(UUID.randomUUID())
                        .accountHolderId(existingHolderId)
                        .accountType(AccountType.CURRENT)
                        .openingBalance(new BigDecimal("100.00"))
                        .build(),
                OpenAccountCommand.builder()
                        .id(UUID.randomUUID())
                        .accountHolderId(UUID.randomUUID().toString())
                        .accountType(AccountType.CURRENT)
                        .openingBalance(new BigDecimal("200.00"))
                        .build(),
                OpenAccountCommand.builder()
                        .id(UUID.randomUUID())
                        .accountHolderId(UUID.randomUUID().toString())
                        .accountType(AccountType.CURRENT)
                        .openingBalance(new BigDecimal("500.00"))
                        .build()
        );

        commands.forEach(command -> commandGateway.send(command));

        await()
                .timeout(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(accountRepository.count())
                        .isEqualTo(initialCount + 3));
    }
}
