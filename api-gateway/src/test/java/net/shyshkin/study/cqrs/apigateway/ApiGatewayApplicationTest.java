package net.shyshkin.study.cqrs.apigateway;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.apigateway.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.apigateway.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.core.models.User;
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

    static User existingUser;

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

    @Test
    @Order(74)
    void getAllUsers_ok_dto() {

        //when
        webTestClient.get().uri("/api/v1/users")
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(UserLookupResponse.class)
                .value(userLookupResponse -> assertThat(userLookupResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .satisfies(response -> assertThat(response.getUsers())
                                .hasSizeGreaterThanOrEqualTo(2)
                                .allSatisfy(user -> assertThat(user)
                                        .hasNoNullFieldsOrProperties()
                                        .satisfies(u -> existingUser = u)
                                        .satisfies(u -> assertThat(u.getAccount())
                                                .hasNoNullFieldsOrProperties()
                                                .satisfies(account -> assertThat(account.getUsername()).contains("shyshkin")))))
                );
    }

    @Test
    @Order(78)
    void getUserById() {

        //given
        String userId = existingUser.getId();

        //when
        webTestClient.get().uri("/api/v1/users/{userId}", userId)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(UserLookupResponse.class)
                .value(userLookupResponse -> assertThat(userLookupResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .satisfies(response -> assertThat(response.getUsers())
                                .hasSize(1)
                                .allSatisfy(user -> assertThat(user)
                                        .hasNoNullFieldsOrProperties()
                                        .hasFieldOrPropertyWithValue("id", userId)
                                        .satisfies(u -> assertThat(u.getAccount())
                                                .hasNoNullFieldsOrProperties())))
                );
    }

}
