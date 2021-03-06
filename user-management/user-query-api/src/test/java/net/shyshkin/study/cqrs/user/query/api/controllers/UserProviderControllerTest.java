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
import net.shyshkin.study.cqrs.user.query.api.dto.VerificationPasswordResponse;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        "logging.level.org.springframework.web.servlet.DispatcherServlet=debug",
        "logging.level.org.springframework.data.mongodb.core.MongoTemplate=debug"
})
class UserProviderControllerTest extends AbstractDockerComposeTest {

    RestTemplate userCmdApiRestTemplate;

    @Autowired
    UserRepository repository;

    static User existingUser = null;

    private static String jwtReadAccessToken = null;

    private static final AtomicReference<String> lastRawPassword = new AtomicReference<>("");

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

    @Nested
    class FindByEmailTests {

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
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("email", emailAddress)
                    .hasFieldOrPropertyWithValue("username", newUser.getAccount().getUsername())
                    .hasFieldOrPropertyWithValue("roles", newUser.getAccount().getRoles())
                    .hasFieldOrPropertyWithValue("firstname", newUser.getFirstname())
                    .hasFieldOrPropertyWithValue("lastname", newUser.getLastname());
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
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("email", emailAddress)
                    .hasFieldOrPropertyWithValue("username", newUser.getAccount().getUsername())
                    .hasFieldOrPropertyWithValue("roles", newUser.getAccount().getRoles())
                    .hasFieldOrPropertyWithValue("firstname", newUser.getFirstname())
                    .hasFieldOrPropertyWithValue("lastname", newUser.getLastname());
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
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(responseEntity.getBody()).isNull();
        }

        @Test
        @Order(40)
        void getUserByEmail_validationFail() {

            //given
            String email = "not_an_email_pattern_AT_test.com";

            //when
            var responseEntity = restTemplate.getForEntity("/email/{email}", BaseResponse.class, email);

            //then
            log.debug("Response entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrProperty("message")
                    .satisfies(response -> assertThat(response.getMessage()).contains("getUserByEmail.email", "Provide correct email address"));
        }
    }

    @Nested
    @TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
    class VerifyByEmailAndPasswordTests {

        @Test
        @Order(10)
        void verifyEmailPassword_createNew_withToken() {

            //given
            User newUser = registerNewUser();
            String emailAddress = newUser.getEmailAddress();
            String password = lastRawPassword.get();
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/email/{email}/verify-password", password, VerificationPasswordResponse.class, emailAddress);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", true);
        }

        @Test
        @Order(20)
        void verifyEmailPassword_createNew_withoutToken() {

            //given
            User newUser = registerNewUser();
            String emailAddress = newUser.getEmailAddress();
            String password = lastRawPassword.get();

            restTemplate = new TestRestTemplate(restTemplateBuilder
                    .rootUri("http://localhost:" + randomServerPort + "/api/v1/users/provider"));
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/email/{email}/verify-password", password, VerificationPasswordResponse.class, emailAddress);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", true);
        }

        @Test
        @Order(30)
        void verifyEmailPassword_wrongPassword() {

            //given
            User newUser = registerNewUser();
            String emailAddress = newUser.getEmailAddress();
            String password = "s0me_Fake_P@s5w0rd";
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/email/{email}/verify-password", password, VerificationPasswordResponse.class, emailAddress);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", false);
        }

        @Test
        @Order(40)
        void verifyEmailPassword_absent() {

            //given
            String email = "absent@test.com";
            String password = lastRawPassword.get();
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/email/{email}/verify-password", password, VerificationPasswordResponse.class, email);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", false);
        }

        @Test
        @Order(50)
        void verifyEmailPassword_validationFail() {

            //given
            String email = "not_an_email_pattern_AT_test.com";
            String password = lastRawPassword.get();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/email/{email}/verify-password", password, BaseResponse.class, email);

            //then
            log.debug("Response entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrProperty("message")
                    .satisfies(response -> assertThat(response.getMessage()).contains(".email", "Provide correct email address"));
        }
    }

    @Nested
    class FindByUsernameTests {

        @Test
        @Order(10)
        void getUserByUsername_createNew_withToken() {

            //given
            User newUser = registerNewUser();
            String username = newUser.getAccount().getUsername();
            printTestStartBorder();

            //when
            var responseEntity = restTemplate.getForEntity("/username/{username}", UserProviderResponse.class, username);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("email", newUser.getEmailAddress())
                    .hasFieldOrPropertyWithValue("username", newUser.getAccount().getUsername())
                    .hasFieldOrPropertyWithValue("roles", newUser.getAccount().getRoles())
                    .hasFieldOrPropertyWithValue("firstname", newUser.getFirstname())
                    .hasFieldOrPropertyWithValue("lastname", newUser.getLastname());
        }

        @Test
        @Order(20)
        void getUserByUsername_createNew_withoutToken() {

            //given
            User newUser = registerNewUser();
            String username = newUser.getAccount().getUsername();

            restTemplate = new TestRestTemplate(restTemplateBuilder
                    .rootUri("http://localhost:" + randomServerPort + "/api/v1/users/provider"));
            printTestStartBorder();

            //when
            var responseEntity = restTemplate.getForEntity("/username/{username}", UserProviderResponse.class, username);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("email", newUser.getEmailAddress())
                    .hasFieldOrPropertyWithValue("username", newUser.getAccount().getUsername())
                    .hasFieldOrPropertyWithValue("roles", newUser.getAccount().getRoles())
                    .hasFieldOrPropertyWithValue("firstname", newUser.getFirstname())
                    .hasFieldOrPropertyWithValue("lastname", newUser.getLastname());
        }

        @Test
        @Order(30)
        void getUserByUsername_absent() {

            //given
            String username = "absent.user";
            printTestStartBorder();

            //when
            var responseEntity = restTemplate.getForEntity("/username/{username}", BaseResponse.class, username);

            //then
            log.debug("Response entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(responseEntity.getBody()).isNull();
        }

        @ParameterizedTest
        @Order(40)
        @CsvSource({
                "hi",
                "TOO_LONG_!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        })
        void getUserByUsername_validationFail(String username) {

            //given
            printTestStartBorder();

            //when
            var responseEntity = restTemplate.getForEntity("/username/{username}", BaseResponse.class, username);

            //then
            log.debug("Response entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrProperty("message")
                    .satisfies(response -> assertThat(response.getMessage())
                            .contains("getUserByUsername.username", "Username must be from 3 to 255 characters long"));
        }
    }

    @Nested
    @TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
    class VerifyByUsernameAndPasswordTests {

        @Test
        @Order(10)
        void verifyUsernamePassword_createNew_withToken() {

            //given
            User newUser = registerNewUser();
            String username = newUser.getAccount().getUsername();
            String password = lastRawPassword.get();
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/username/{username}/verify-password", password, VerificationPasswordResponse.class, username);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", true);
        }

        @Test
        @Order(20)
        void verifyUsernamePassword_createNew_withoutToken() {

            //given
            User newUser = registerNewUser();
            String username = newUser.getAccount().getUsername();
            String password = lastRawPassword.get();

            restTemplate = new TestRestTemplate(restTemplateBuilder
                    .rootUri("http://localhost:" + randomServerPort + "/api/v1/users/provider"));
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/username/{username}/verify-password", password, VerificationPasswordResponse.class, username);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", true);
        }

        @Test
        @Order(30)
        void verifyUsernamePassword_wrongPassword() {

            //given
            User newUser = registerNewUser();
            String username = newUser.getAccount().getUsername();
            String password = "s0me_Fake_P@s5w0rd";
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/username/{username}/verify-password", password, VerificationPasswordResponse.class, username);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", false);
        }

        @Test
        @Order(40)
        void verifyUsernamePassword_absent() {

            //given
            String username = "absent.user";
            String password = lastRawPassword.get();
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/username/{username}/verify-password", password, VerificationPasswordResponse.class, username);

            //then
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(responseEntity.getBody())
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrPropertyWithValue("valid", false);
        }

        @ParameterizedTest
        @Order(40)
        @CsvSource({
                "hi",
                "TOO_LONG_!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        })
        void verifyUsernamePassword_validationFail(String username) {

            //given
            String password = lastRawPassword.get();
            printTestStartBorder();

            //when
            var responseEntity = restTemplate
                    .postForEntity("/username/{username}/verify-password", password, BaseResponse.class, username);

            //then
            log.debug("Response entity: {}", responseEntity);
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(responseEntity.getBody())
                    .isNotNull()
                    .hasNoNullFieldsOrProperties()
                    .hasFieldOrProperty("message")
                    .satisfies(response -> assertThat(response.getMessage())
                            .contains(".username", "Username must be from 3 to 255 characters long"));
        }
    }

    User registerNewUser() {

        //given
        var newUser = createNewUser();
        lastRawPassword.set(newUser.getAccount().getPassword());

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

    private void printTestStartBorder() {
        System.out.println("                                                         ");
        System.out.println("----------------------TEST STARTING----------------------");
        System.out.println("                                                         ");
    }
}