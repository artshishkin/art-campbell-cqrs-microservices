package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
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
class RegisterUserControllerAuthorizationTest extends AbstractDockerComposeTest {

    @BeforeAll
    static void beforeAll() {
        jwtAccessToken = null;
    }

    @BeforeEach
    void setUp() {
        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkina.kate", "P@ssW0rd1");

//        restTemplate = new TestRestTemplate(restTemplateBuilder
//                .defaultHeader(AUTHORIZATION, "Bearer " + jwtAccessToken)
//                .rootUri("http://localhost:" + randomServerPort));
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
}