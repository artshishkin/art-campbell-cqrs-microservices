package net.shyshkin.study.cqrs.user.query.api.handlers;

import com.github.javafaker.Faker;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import net.shyshkin.study.cqrs.user.query.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.query.api.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Disabled("Need to use jwtAccessToken")
class UserEventHandlerTest extends AbstractDockerComposeTest {

    private static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    RestTemplate userCmdApiRestTemplate;

    @Autowired
    UserRepository repository;

    static User existingUser = null;

    @BeforeEach
    void setUp() {

        String host = composeContainer.getUserCmdApiHost();
        Integer port = composeContainer.getUserCmdApiPort();
        String rootUri = String.format("http://%s:%d/api/v1/users", host, port);
        log.debug("User Command API Root URI: {}", rootUri);

        userCmdApiRestTemplate = restTemplateBuilder
                .rootUri(rootUri)
                .build();
    }

    @Test
    @Order(10)
    void onUserRegisteredEvent() {

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
    }

    @Test
    @Order(20)
    void onUserUpdatedEvent() {

        //given
        if (existingUser == null)
            onUserRegisteredEvent();

        var updatedUser = new UserCreateDto();
        updatedUser.setAccount(new AccountDto());
        updatedUser.getAccount().setRoles(new ArrayList<>());
        var userId = existingUser.getId();
        BeanUtils.copyProperties(existingUser, updatedUser, "id");
        BeanUtils.copyProperties(existingUser.getAccount(), updatedUser.getAccount());

        log.debug("user dto: {}", updatedUser);
        updatedUser.setFirstname(updatedUser.getFirstname().toUpperCase());
        updatedUser.getAccount().setUsername(updatedUser.getAccount().getUsername().toUpperCase());
        updatedUser.getAccount().setPassword(generatePassword());

        //when
        RequestEntity<UserCreateDto> requestEntity = RequestEntity.put("/{id}", userId).body(updatedUser);
        ResponseEntity<BaseResponse> responseEntity = userCmdApiRestTemplate
                .exchange(requestEntity, BaseResponse.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        BaseResponse body = responseEntity.getBody();
        assertThat(body.getMessage()).isEqualTo("User updated successfully");

        await()
                .timeout(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertThat(repository.findById(userId))
                            .hasValueSatisfying(user -> assertThat(user)
                                    .hasFieldOrPropertyWithValue("id", userId)
                                    .isEqualToIgnoringGivenFields(updatedUser, "id", "account"))
                            .hasValueSatisfying(user -> assertThat(user.getAccount())
                                    .isEqualToIgnoringGivenFields(updatedUser.getAccount(), "password")
                                    .satisfies(account -> assertThat(account.getPassword()).startsWith("{bcrypt}")))
                            .hasValueSatisfying(user -> log.debug("Updated user: {}", user))
                    ;
                });
    }

    @Test
    @Order(30)
    void onUserRemovedEvent() {

        //given
        if (existingUser == null)
            onUserRegisteredEvent();

        var userId = existingUser.getId();

        //when
        var requestEntity = RequestEntity.delete("/{id}", userId).build();
        ResponseEntity<BaseResponse> responseEntity = userCmdApiRestTemplate
                .exchange(requestEntity, BaseResponse.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        BaseResponse body = responseEntity.getBody();
        assertThat(body.getMessage()).isEqualTo("User removed successfully");

        await()
                .timeout(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(repository.existsById(userId)).isFalse());
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
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BaseResponse {
        private String message;
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