package net.shyshkin.study.cqrs.user.oauth2_0;

import com.github.javafaker.Faker;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.dto.OAuthResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("For manual test only -> start `/infrastructure/docker-compose.yml`, user-cmd-api and user-query-api first")
class UserOauth20ApplicationManualWithRegisteringUserTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    RestTemplate userCmdApiRestTemplate;

    RestTemplate userQueryApiRestTemplate;

    static User existingUser = null;

    private String plainPassword;

    @Value("${app.security.oauth2.client.client-id}")
    private String clientId;

    @Value("${app.security.oauth2.client.client-secret}")
    private String clientSecret;

    @BeforeEach
    void setUp() {
        userCmdApiRestTemplate = restTemplateBuilder.rootUri("http://localhost:8091/api/v1/users").build();
        userQueryApiRestTemplate = restTemplateBuilder.rootUri("http://localhost:8092/api/v1/users").build();
    }

    @Test
    void getAccessTokenFromOAuth20Server() {
        //given
        User user = getRandomUser();

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", user.getAccount().getUsername());
        map.add("password", plainPassword);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
        log.debug("Request: {}", requestEntity);

        var responseEntity = restTemplate
                .withBasicAuth(clientId, clientSecret)
                .postForEntity("/oauth/token", requestEntity, OAuthResponse.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasNoNullFieldsOrProperties();

    }

    User getRandomUser() {
        return registerNewUser();
    }

    User registerNewUser() {

        //given
        var newUser = createNewUser();
        plainPassword = newUser.getAccount().getPassword();

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
                    try {
                        assertThat(userQueryApiRestTemplate.getForEntity("/{userId}", UserLookupResponse.class, userId))
                                .satisfies(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK))
                                .satisfies(entity -> assertThat(entity.getBody().getUsers())
                                        .hasSize(1)
                                        .allSatisfy(user -> {
                                            assertThat(user.getId()).isEqualTo(userId);
                                            existingUser = user;
                                        })
                                );
                    } catch (Exception ex) {
                        log.debug("Exception occurred {}:{}", ex.getClass().getName(), ex.getMessage());
                        assertThat(true).isFalse();
                    }
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

    @Data
    private static class UserLookupResponse {
        private List<User> users;
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
