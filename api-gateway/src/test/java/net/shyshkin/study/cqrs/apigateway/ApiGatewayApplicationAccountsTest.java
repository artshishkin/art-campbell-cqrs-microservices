package net.shyshkin.study.cqrs.apigateway;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.apigateway.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.apigateway.dto.accounts.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiGatewayApplicationAccountsTest extends AbstractDockerComposeTest {

    @Autowired
    ApplicationContext applicationContext;

    private static UUID existingAccountId;
    private static UUID existingHolderId;

    @BeforeEach
    void setUp() {

        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");

        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .configureClient()
                .defaultHeaders(headers -> headers.setBearerAuth(jwtAccessToken))
                .build();
    }

    @Test
    @Order(10)
    void findAllAccounts_empty() {

        //when
        webTestClient.get().uri("/api/v1/accounts")
                .exchange()

                //then
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    @Order(15)
    void findAccountById_empty() {

        //given
        UUID id = UUID.randomUUID();
        String expectedMessage = String.format("Bank Account with id `%s` not found", id);

        //when
        webTestClient.get().uri("/api/v1/accounts/{id}", id)
                .exchange()

                //then
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedMessage);
    }

    @Test
    @Order(20)
    void findAccountsByHolderId_empty() {

        //given
        UUID holderId = UUID.randomUUID();

        //when
        webTestClient.get().uri("/api/v1/accounts?accountHolderId={holderId}", holderId)
                .exchange()

                //then
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    @Order(25)
    void findAccountsByBalance_empty() {

        //when
        webTestClient.get().uri("/api/v1/accounts?equalityType=LESS_THEN&balance=100")
                .exchange()

                //then
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();
    }

    @Test
    @Order(100)
    void openAccount_OK() {

        //given
        existingHolderId = UUID.randomUUID();
        var command = OpenAccountCommand.builder()
                .accountHolderId(existingHolderId.toString())
                .accountType(AccountType.CURRENT)
                .openingBalance(new BigDecimal("123.56"))
                .build();

        //when
        webTestClient.post().uri("/api/v1/accounts")
                .bodyValue(command)
                .exchange()

                //then
                .expectStatus().isCreated()
                .expectBody(OpenAccountResponse.class)
                .value(registerUserResponse -> assertThat(registerUserResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "Account opened successfully")
                        .satisfies(response -> existingAccountId = response.getId())
                );
    }

    @ParameterizedTest
    @CsvSource({
            "deposits,Funds deposited successfully",
            "withdrawals,Funds withdrawn successfully",
    })
    @Order(110)
    void depositOrWithdrawFunds_ok(String endpoint, String expectedMessage) {

        //given
        UUID id = existingAccountId;
        var command = FundsCommand.builder()
                .id(id)
                .amount(new BigDecimal("10.00"))
                .build();

        //when
        webTestClient.put().uri("/api/v1/accounts/{id}/{endpoint}", id, endpoint)
                .bodyValue(command)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(baseResponse -> assertThat(baseResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", expectedMessage)
                );
    }

    @DisplayName("When depositing or withdrawing funds Id in URL and in Command should match")
    @ParameterizedTest(name = "[{index} {arguments}]")
    @ValueSource(strings = {"deposits", "withdrawals"})
    @Order(120)
    void depositOrWithdrawFunds_wrongId(String endpoint) {

        //given
        UUID id = existingAccountId;
        var command = FundsCommand.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("10.00"))
                .build();

        String expectedMessage = String.format("Account Id in URL `%s` does not match Id in command `%s`", id, command.getId());

        //when
        webTestClient.put().uri("/api/v1/accounts/{id}/{endpoint}", id, endpoint)
                .bodyValue(command)
                .exchange()

                //then
                .expectStatus().isBadRequest()
                .expectBody(BaseResponse.class)
                .value(baseResponse -> assertThat(baseResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", expectedMessage)
                );
    }

    @DisplayName("When depositing or withdrawing funds amount should be valid")
    @ParameterizedTest(name = "[{index} {arguments}]")
    @ValueSource(strings = {"deposits", "withdrawals"})
    @Order(125)
    void depositOrWithdrawFunds_validationFailed(String endpoint) {

        //given
        UUID id = existingAccountId;
        var command = FundsCommand.builder()
                .id(id)
                .amount(new BigDecimal("-10.00"))
                .build();

        //when
        webTestClient.put().uri("/api/v1/accounts/{id}/{endpoint}", id, endpoint)
                .bodyValue(command)
                .exchange()

                //then
                .expectStatus().isBadRequest()
                .expectBody(BaseResponse.class)
                .value(baseResponse -> assertThat(baseResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .satisfies(body -> assertThat(body.getMessage())
                                .contains("Validation failed for argument ")
                                .contains("Amount must be positive")
                        )
                );
    }

    @Test
    @Order(126)
    void findAllAccounts_presentOne() {

        //given
        String expectedMessage = "Successfully returned 1 Bank Account(s)";

        //when
        webTestClient.get().uri("/api/v1/accounts")
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedMessage)
                .jsonPath("$.accounts").isArray()
                .jsonPath("$.accounts[1]").doesNotExist()
                .jsonPath("$.accounts[0].id").isEqualTo(existingAccountId.toString())
                .jsonPath("$.accounts[0].accountHolderId").isEqualTo(existingHolderId.toString());
    }

    @Test
    @Order(126)
    void findAccountsById_presentOne() {

        //given
        UUID id = existingAccountId;
        String expectedMessage = "Bank Account successfully returned";

        //when
        webTestClient.get().uri("/api/v1/accounts/{id}", id)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedMessage)
                .jsonPath("$.accounts").isArray()
                .jsonPath("$.accounts[1]").doesNotExist()
                .jsonPath("$.accounts[0].id").isEqualTo(existingAccountId.toString())
                .jsonPath("$.accounts[0].accountHolderId").isEqualTo(existingHolderId.toString());
    }

    @Test
    @Order(126)
    void findAccountsByHolderId_presentOne() {

        //given
        UUID holderId = existingHolderId;
        String expectedMessage = "Successfully returned 1 Bank Account(s)";

        //when
        webTestClient.get().uri("/api/v1/accounts?accountHolderId={holderId}", holderId)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedMessage)
                .jsonPath("$.accounts").isArray()
                .jsonPath("$.accounts[1]").doesNotExist()
                .jsonPath("$.accounts[0].id").isEqualTo(existingAccountId.toString())
                .jsonPath("$.accounts[0].accountHolderId").isEqualTo(existingHolderId.toString());
    }

    @Test
    @Order(126)
    void findAccountsByBalance_presentOne() {

        //given
        String expectedMessage = "Successfully returned 1 Bank Account(s)";

        //when
        webTestClient.get().uri("/api/v1/accounts?equalityType=LESS_THEN&balance=200")
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedMessage)
                .jsonPath("$.accounts").isArray()
                .jsonPath("$.accounts[1]").doesNotExist()
                .jsonPath("$.accounts[0].id").isEqualTo(existingAccountId.toString())
                .jsonPath("$.accounts[0].accountHolderId").isEqualTo(existingHolderId.toString());
    }

    @Test
    @Order(130)
    void closeAccount_OK() {

        //given
        UUID id = existingAccountId;

        //when
        webTestClient.delete().uri("/api/v1/accounts/{id}", id)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(response -> assertThat(response)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "Account closed successfully")
                );
    }

    @Test
    @Order(135)
    void closeAccount_deleted() {

        //given
        UUID id = existingAccountId;
        String expectedMessage = String.format("Aggregate with identifier [%s] not found. It has been deleted.", id);

        //when
        webTestClient.delete().uri("/api/v1/accounts/{id}", id)
                .exchange()

                //then
                .expectStatus().isNotFound()
                .expectBody(BaseResponse.class)
                .value(response -> assertThat(response)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", expectedMessage)
                );
    }

    @Test
    @Order(140)
    void closeAccount_absent() {

        //given
        UUID id = UUID.randomUUID();

        //when
        webTestClient.delete().uri("/api/v1/accounts/{id}", id)
                .exchange()

                //then
                .expectStatus().isNotFound()
                .expectBody(BaseResponse.class)
                .value(response -> assertThat(response)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "The aggregate was not found in the event store")
                );
    }

}
