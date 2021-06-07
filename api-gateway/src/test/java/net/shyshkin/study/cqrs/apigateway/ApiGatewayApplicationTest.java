package net.shyshkin.study.cqrs.apigateway;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.apigateway.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.apigateway.dto.RegisterUserResponse;
import net.shyshkin.study.cqrs.apigateway.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiGatewayApplicationTest extends AbstractDockerComposeTest {

    @Autowired
    ApplicationContext applicationContext;

    static User existingUser;
    private static UserCreateDto userDto;
    private static String existingUserDtoId;

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

    @Test
    @Order(82)
    void searchUser() {

        //given
        String filter = "ate";

        //when
        webTestClient.get().uri("/api/v1/users/search/{filter}", filter)
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
                                        .hasFieldOrPropertyWithValue("emailAddress", "kate.shishkina@gmail.com")
                                        .satisfies(u -> assertThat(u.getAccount())
                                                .hasNoNullFieldsOrProperties()
                                                .hasFieldOrPropertyWithValue("username", "shyshkina.kate"))))
                );
    }

    @Test
    @Order(86)
    void registerUser() {

        //given
        userDto = createNewUser();

        //when
        webTestClient.post().uri("/api/v1/users")
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .bodyValue(userDto)
                .exchange()

                //then
                .expectStatus().isCreated()
                .expectBody(RegisterUserResponse.class)
                .value(registerUserResponse -> assertThat(registerUserResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "User registered successfully")
                        .satisfies(response -> existingUserDtoId = response.getId())
                );
    }

    @Test
    @Order(90)
    void updateUser() {

        //given
        userDto.setFirstname(userDto.getFirstname().toUpperCase());

        //when
        webTestClient.put().uri("/api/v1/users/{userId}", existingUserDtoId)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .bodyValue(userDto)
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(baseResponse -> assertThat(baseResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "User updated successfully")
                );
    }

    @Test
    @Order(94)
    void removeUser() {

        //when
        webTestClient.delete().uri("/api/v1/users/{userId}", existingUserDtoId)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .exchange()

                //then
                .expectStatus().isOk()
                .expectBody(BaseResponse.class)
                .value(baseResponse -> assertThat(baseResponse)
                        .satisfies(body -> log.debug("Response body: {}", body))
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrPropertyWithValue("message", "User removed successfully")
                );
    }

    private UserCreateDto createNewUser() {
        var accountDto = AccountDto.builder()
                .username(FAKER.name().username())
                .password(generatePassword())
                .roles(List.of(Role.READ_PRIVILEGE))
                .build();

        return UserCreateDto.builder()
                .firstname(FAKER.name().firstName())
                .lastname(FAKER.name().lastName())
                .emailAddress(FAKER.bothify("????##@gmail.com"))
                .account(accountDto)
                .build();
    }

    private String generatePassword() {
        return FAKER.regexify("[a-z]{6}[1-9]{6}[A-Z]{6}[!@#&()]{2}");
    }

}
