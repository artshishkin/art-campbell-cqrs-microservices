package net.shyshkin.study.cqrs.user.cmd.api.controllers;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commands.RegisterUserCommand;
import net.shyshkin.study.cqrs.user.cmd.api.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.core.dto.BaseResponse;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RemoveUserControllerTest extends AbstractDockerComposeTest {

    @Autowired
    UserMapper mapper;

    static User existingUser = null;

    @BeforeEach
    void setUp() {
        if (jwtAccessToken == null)
            jwtAccessToken = getJwtAccessToken("shyshkin.art", "P@ssW0rd!");

        restTemplate = new TestRestTemplate(restTemplateBuilder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAccessToken)
                .rootUri("http://localhost:" + randomServerPort));
    }

    @Test
    void removeUser_success() {
        //given
        User user = getRandomUser();
        String userId = user.getId();

        //when
        ResponseEntity<BaseResponse> responseEntity = restTemplate
                .exchange("/api/v1/users/{id}", HttpMethod.DELETE, null, BaseResponse.class, userId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var baseResponse = responseEntity.getBody();
        assertThat(baseResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("message", "User removed successfully");
    }

    @Test
    void removeUser_absent() {
        //given
        String userId = UUID.randomUUID().toString();

        //when
        ResponseEntity<BaseResponse> responseEntity = restTemplate
                .exchange("/api/v1/users/{id}", HttpMethod.DELETE, null, BaseResponse.class, userId);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var baseResponse = responseEntity.getBody();
        assertThat(baseResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("message", "The aggregate was not found in the event store");
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