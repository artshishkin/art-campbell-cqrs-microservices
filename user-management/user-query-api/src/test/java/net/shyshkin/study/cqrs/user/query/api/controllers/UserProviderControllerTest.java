package net.shyshkin.study.cqrs.user.query.api.controllers;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import net.shyshkin.study.cqrs.user.query.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.query.api.dto.UserProviderResponse;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class UserProviderControllerTest extends AbstractDockerComposeTest {

    RestTemplate userCmdApiRestTemplate;

    @Autowired
    UserRepository repository;

    static User existingUser = null;

    private static String jwtReadAccessToken = null;

    @BeforeEach
    void setUp() {

        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");

        if (jwtReadAccessToken == null)
            jwtReadAccessToken = getJwtAccessToken("shyshkina.kate", "P@ssW0rd1");

        String host = composeContainer.getUserCmdApiHost();
        Integer port = composeContainer.getUserCmdApiPort();
        String rootUri = String.format("http://%s:%d/api/v1/users", host, port);
        log.debug("User Command API Root URI: {}", rootUri);

        userCmdApiRestTemplate = restTemplateBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .rootUri(rootUri)
                .build();

        restTemplate = new TestRestTemplate(restTemplateBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtReadAccessToken)
                .rootUri("http://localhost:" + randomServerPort + "/api/v1/users/provider"));
    }

    @Test
    @Order(10)
    void getUserByEmail_createNew_withToken() {

        //given
        User newUser = registerNewUser();
        String emailAddress = newUser.getEmailAddress();

        //when
        var responseEntity = restTemplate.getForEntity("/email/{email}", UserProviderResponse.class, emailAddress);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getUsers())
                .hasSize(1)
                .allSatisfy(user -> assertThat(user)
                        .hasFieldOrPropertyWithValue("emailAddress", emailAddress)
                        .isEqualToIgnoringGivenFields(newUser, "id", "account"))
                .allSatisfy(user -> assertThat(user.getAccount())
                        .isEqualToIgnoringGivenFields(newUser.getAccount(), "password")
                        .satisfies(account -> assertThat(account.getPassword()).startsWith("{bcrypt}")));
    }

    @Test
    @Order(20)
    void getUserByEmail_createNew_withoutToken() {

        //given
        User newUser = registerNewUser();
        String emailAddress = newUser.getEmailAddress();

        restTemplate = new TestRestTemplate(restTemplateBuilder
                .rootUri("http://localhost:" + randomServerPort + "/api/v1/users/provider"));

        //when
        var responseEntity = restTemplate.getForEntity("/email/{email}", UserProviderResponse.class, emailAddress);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getUsers())
                .hasSize(1)
                .allSatisfy(user -> assertThat(user)
                        .hasFieldOrPropertyWithValue("emailAddress", emailAddress)
                        .isEqualToIgnoringGivenFields(newUser, "id", "account"))
                .allSatisfy(user -> assertThat(user.getAccount())
                        .isEqualToIgnoringGivenFields(newUser.getAccount(), "password")
                        .satisfies(account -> assertThat(account.getPassword()).startsWith("{bcrypt}")));
    }

    @Test
    @Order(30)
    void getUserByEmail_absent() {

        //given
        String email = "absent@test.com";

        //when
        var responseEntity = restTemplate.getForEntity("/email/{email}", BaseResponse.class, email);

        //then
        log.debug("Response entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage())
                .contains("User not found");
    }


    User registerNewUser() {

        //given
        var newUser = createNewUser();

        //when
        ResponseEntity<RegisterUserResponse> responseEntity = null;
        try {
            responseEntity = userCmdApiRestTemplate.postForEntity("/", newUser, RegisterUserResponse.class);
        } catch (HttpClientErrorException exception) {
            log.debug("Response headers: {}", exception.getResponseHeaders());
            exception.printStackTrace();
            assertThat(exception.getMessage()).isEqualTo("There was unexpected exception so test failed");
        }

        //then
        log.debug("Response entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        RegisterUserResponse body = responseEntity.getBody();
        assertThat(body.getMessage()).isEqualTo("User registered successfully");
        String userId = body.getId();
        assertThat(userId).isNotEmpty();

        await()
                .timeout(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertThat(repository.findById(userId))
                            .hasValueSatisfying(user -> assertThat(user)
                                    .hasFieldOrPropertyWithValue("id", userId)
                                    .isEqualToIgnoringGivenFields(newUser, "id", "account"))
                            .hasValueSatisfying(user -> assertThat(user.getAccount())
                                    .isEqualToIgnoringGivenFields(newUser.getAccount(), "password")
                                    .satisfies(account -> assertThat(account.getPassword()).startsWith("{bcrypt}")))
                            .hasValueSatisfying(user -> {
                                log.debug("Registered user: {}", user);
                                existingUser = user;
                            })
                    ;
                });
        return existingUser;
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

    @Getter
    @ToString
    private static class RegisterUserResponse extends BaseResponse {

        private final String id;

        public RegisterUserResponse(String id, String message) {
            super(message);
            this.id = id;
        }
    }
}