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
import net.shyshkin.study.cqrs.user.query.api.dto.UserLookupResponse;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Slf4j
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class UserLookupControllerTest extends AbstractDockerComposeTest {

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
                .rootUri("http://localhost:" + randomServerPort));
    }

    @Test
    @Order(50)
    void getUserById_present() {

        //given
        User newUser = registerNewUser();
        String userId = newUser.getId();

        //when
        var responseEntity = restTemplate.getForEntity("/api/v1/users/{id}", UserLookupResponse.class, userId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getUsers())
                .hasSize(1)
                .allSatisfy(user -> assertThat(user)
                        .hasFieldOrPropertyWithValue("id", userId)
                        .isEqualToIgnoringGivenFields(newUser, "id", "account"))
                .allSatisfy(user -> assertThat(user.getAccount())
                        .isEqualToIgnoringGivenFields(newUser.getAccount(), "password")
                        .satisfies(account -> assertThat(account.getPassword()).startsWith("{bcrypt}")));
    }

    @Test
    @Order(52)
    void getUserById_absent() {

        //given
        String userId = UUID.randomUUID().toString();

        //when
        var responseEntity = restTemplate.getForEntity("/api/v1/users/{id}", BaseResponse.class, userId);

        //then
        log.debug("Response entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage())
                .contains("User not found");
    }

    @Test
    @Order(60)
    void getAllUsers() {

        //given
        User newUser = registerNewUser();
        long usersCount = repository.count();

        //when
        var responseEntity = restTemplate.getForEntity("/api/v1/users", UserLookupResponse.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getUsers())
                .hasSize((int) usersCount)
                .allSatisfy(user -> assertThat(user)
                        .hasNoNullFieldsOrProperties()
                        .hasFieldOrProperty("id"))
                .allSatisfy(user -> assertThat(user.getAccount())
                        .satisfies(account -> assertThat(account.getPassword()).startsWith("{bcrypt}")))
                .allSatisfy(user -> log.debug("User retrieved by Http Request: {}", user));
    }

    private static Stream<Arguments> searchTest() {
        return Stream.of(
                arguments("firstname", (Function<User, String>) User::getFirstname),
                arguments("lastname", (Function<User, String>) User::getLastname),
                arguments("emailAddress", (Function<User, String>) User::getEmailAddress),
                arguments("account.username", (Function<User, String>) user -> user.getAccount().getUsername())
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource
    @DisplayName("Searching for phrase that can be met in:")
    void searchTest(String fieldSearch, Function<User, String> filterFunc) {

        //given
        if (existingUser == null)
            registerNewUser();
        String fieldValue = filterFunc.apply(existingUser);
        String filter = fieldValue.substring(1, fieldValue.length() - 1);
        log.debug("Searching for `{}` that can be met in `{}`", filter, fieldSearch);

        //when
        var responseEntity = restTemplate.getForEntity("/api/v1/users/search/{filter}", UserLookupResponse.class, filter);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getUsers())
                .hasSizeGreaterThanOrEqualTo(1)
                .allSatisfy(user -> assertThat(user.getFirstname() + user.getLastname() + user.getEmailAddress() + user.getAccount().getUsername()).contains(filter))
                .allSatisfy(user -> log.debug("Search user: {}", user));
    }

    @Test
    void searchTest_absent() {

        //given
        String filter = UUID.randomUUID().toString();

        //when
        var responseEntity = restTemplate.getForEntity("/api/v1/users/search/{filter}", BaseResponse.class, filter);

        //then
        log.debug("Response Entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    User registerNewUser() {

        //given
        var newUser = createNewUser();

        //when
        var responseEntity = userCmdApiRestTemplate.postForEntity("/", newUser, RegisterUserResponse.class);

        //then
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