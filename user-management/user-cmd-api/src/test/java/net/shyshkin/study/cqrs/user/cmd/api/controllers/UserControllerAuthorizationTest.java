package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
class UserControllerAuthorizationTest extends AbstractDockerComposeTest {

    @BeforeAll
    static void beforeAll() {
        jwtAccessToken = null;
    }

    @BeforeEach
    void setUp() {
        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkina.kate", "P@ssW0rd1");
    }

    @AfterAll
    static void afterAll() {
        jwtAccessToken = null;
    }

    @Test
    void registerUser_forbidden() {
        //given
        UserCreateDto dto = createNewUserDto();

        //when
        var requestEntity = RequestEntity.post("/api/v1/users")
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .body(dto);
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"access_denied\",\"error_description\":\"");
    }

    @Test
    void registerUser_unauthorized() {
        //given
        UserCreateDto dto = createNewUserDto();

        //when
        var requestEntity = RequestEntity.post("/api/v1/users")
                .headers(headers -> headers.remove(AUTHORIZATION))
                .body(dto);
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}");
    }

    @Test
    void registerUser_fakeToken() {
        //given
        UserCreateDto dto = createNewUserDto();

        //when
        var requestEntity = RequestEntity.post("/api/v1/users")
                .headers(headers -> headers.setBearerAuth("SomeFakeToken"))
                .body(dto);
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"invalid_token\",\"error_description\":\"Cannot convert access token to JSON\"}");
    }

    @Test
    void updateUser_forbidden() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        UserCreateDto dto = mapper.toDto(user);

        //when
        RequestEntity<UserCreateDto> requestEntity = RequestEntity
                .put("/api/v1/users/{id}", userId)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .body(dto);
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"access_denied\",\"error_description\":\"");
    }

    @Test
    void updateUser_unauthorized() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        UserCreateDto dto = mapper.toDto(user);

        //when
        RequestEntity<UserCreateDto> requestEntity = RequestEntity
                .put("/api/v1/users/{id}", userId)
                .headers(headers -> headers.remove(AUTHORIZATION))
                .body(dto);
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}");
    }

    @Test
    void removeUser_forbidden() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        //when
        var requestEntity = RequestEntity
                .delete("/api/v1/users/{id}", userId)
                .headers(headers -> headers.setBearerAuth(jwtAccessToken))
                .build();
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"access_denied\",\"error_description\":\"");
    }

    @Test
    void removeUser_unauthorized() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        //when
        var requestEntity = RequestEntity
                .delete("/api/v1/users/{id}", userId)
                .headers(headers -> headers.remove(AUTHORIZATION))
                .build();
        var responseEntity = restTemplate
                .exchange(requestEntity, String.class);

        //then
        log.debug("Response: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
        var registerUserResponse = responseEntity.getBody();
        assertThat(registerUserResponse)
                .isNotNull()
                .contains("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}");
    }

    private User getRandomUser() {

        if (existingUser == null) {
            var newUser = createNewUser();
            var registerUserCommand = RegisterUserCommand.builder()
                    .id(newUser.getId())
                    .user(newUser)
                    .build();
            String aggregateId = commandGateway.sendAndWait(registerUserCommand);
            assertThat(aggregateId).isEqualTo(newUser.getId());
            existingUser = newUser;
        }
        return existingUser;
    }

}