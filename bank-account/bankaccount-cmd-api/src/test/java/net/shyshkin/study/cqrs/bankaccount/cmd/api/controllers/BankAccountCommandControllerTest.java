package net.shyshkin.study.cqrs.bankaccount.cmd.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commands.OpenAccountCommand;
import net.shyshkin.study.cqrs.bankaccount.cmd.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.bankaccount.core.dto.OpenAccountResponse;
import net.shyshkin.study.cqrs.bankaccount.core.models.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BankAccountCommandControllerTest extends AbstractDockerComposeTest {

    @BeforeEach
    void setUp() {
        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");

        restTemplate = new TestRestTemplate(restTemplateBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .rootUri("http://localhost:" + randomServerPort));
    }

    @Nested
    class CreateAccountTests {

        @Test
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
                    .hasFieldOrPropertyWithValue("id", id)
                    .hasFieldOrPropertyWithValue("message", expectedMessage)
            ;


        }
    }

}