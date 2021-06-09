package net.shyshkin.study.cqrs.apigateway;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.apigateway.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.apigateway.dto.accounts.*;
import org.junit.jupiter.api.*;
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

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();

        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");
    }

    @Test
    @Order(100)
    void openAccount_OK() {

        //given
        var command = OpenAccountCommand.builder()
                .accountHolderId(UUID.randomUUID().toString())
                .accountType(AccountType.CURRENT)
                .openingBalance(new BigDecimal("123.56"))
                .build();

        //when
        webTestClient.post().uri("/api/v1/accounts")
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
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

    @Test
    @Order(110)
    void depositFunds_ok() {

        //given
        UUID id = existingAccountId;
        var command = FundsCommand.builder()
                .id(id)
                .amount(new BigDecimal("100.00"))
                .build();

        //when
        webTestClient.put().uri("/api/v1/accounts/{id}/deposits", id)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .bodyValue(command)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(baseResponse -> assertThat(baseResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "Funds deposited successfully")
                );
    }

    @Test
    @Order(120)
    void depositFunds_wrongId() {

        //given
        UUID id = existingAccountId;
        var command = FundsCommand.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .build();

        String expectedMessage = String.format("Account Id in URL `%s` does not match Id in command `%s`", id, command.getId());

        //when
        webTestClient.put().uri("/api/v1/accounts/{id}/deposits", id)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
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

}
