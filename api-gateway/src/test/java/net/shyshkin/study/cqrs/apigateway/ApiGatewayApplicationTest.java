package net.shyshkin.study.cqrs.apigateway;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.apigateway.commontest.AbstractDockerComposeTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiGatewayApplicationTest extends AbstractDockerComposeTest {

    @Autowired
    ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();

        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");
    }

    @Test
    @Order(70)
    void getAllUsers_unauthorized() {

        //when
        EntityExchangeResult<byte[]> entityExchangeResult = webTestClient.get().uri("/api/v1/users")
                .headers(headers -> headers.remove(AUTHORIZATION))
                .exchange()

                //then
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("unauthorized")
                .jsonPath("$.error_description").isEqualTo("Full authentication is required to access this resource")
                .returnResult();

        byte[] responseBody = entityExchangeResult.getResponseBody();
        String body = new String(responseBody, StandardCharsets.UTF_8);
        log.debug("Response body: {}", body);
    }

    @Test
    @Order(72)
    void getAllUsers_ok() {

        //when
        EntityExchangeResult<byte[]> entityExchangeResult = webTestClient.get().uri("/api/v1/users")
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.users[0].account.username").value(username -> assertThat((String) username).contains("shyshkin"))
                .jsonPath("$.users[1].account.username").value(username -> assertThat((String) username).contains("shyshkin"))
                .returnResult();

        byte[] responseBody = entityExchangeResult.getResponseBody();
        String body = new String(responseBody, StandardCharsets.UTF_8);
        log.debug("Response body: {}", body);
    }


}
