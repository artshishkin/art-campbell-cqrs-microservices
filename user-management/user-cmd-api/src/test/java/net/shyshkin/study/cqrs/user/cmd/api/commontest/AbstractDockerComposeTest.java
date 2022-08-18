package net.shyshkin.study.cqrs.user.cmd.api.commontest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.mappers.UserMapper;
import net.shyshkin.study.cqrs.user.cmd.api.testcontainers.TestComposeContainer;
import net.shyshkin.study.cqrs.user.core.dto.AccountDto;
import net.shyshkin.study.cqrs.user.core.dto.OAuthResponse;
import net.shyshkin.study.cqrs.user.core.dto.UserCreateDto;
import net.shyshkin.study.cqrs.user.core.models.Account;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "axon.axonserver.servers=${AXON_SERVERS}",
        "spring.data.mongodb.host=${MONGODB_HOST}",
        "spring.data.mongodb.port=${MONGODB_PORT}",
        "app.oauth.uri=${OAUTH_URI}"
})
@Testcontainers
public abstract class AbstractDockerComposeTest {

    protected static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    @Autowired
    protected TestRestTemplate restTemplate;

    @Container
    public static TestComposeContainer composeContainer = TestComposeContainer.getInstance();

    @Autowired
    protected CommandGateway commandGateway;

    @Autowired
    protected RestTemplateBuilder restTemplateBuilder;

    protected static String jwtAccessToken;

    protected static String clientId = "springbankClient";
//    protected static String clientSecret = "674ae476-7591-4078-82e9-5eaea5e71cff";
    protected static String clientSecret = "BJ3cbXk16VWdeevUSYrtVyT2XWurOmey";

    @LocalServerPort
    protected int randomServerPort;

    RestTemplate oauthServerRestTemplate;

    @Autowired
    protected UserMapper mapper;

    protected static User existingUser = null;

    protected String getJwtAccessToken(String username, String plainPassword) {
        oauthServerRestTemplate = restTemplateBuilder
                .basicAuthentication(clientId, clientSecret)
                .rootUri(String.format("http://%s:%d", composeContainer.getOauthHost(), composeContainer.getOauthPort()))
                .build();

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
//        map.add("client_id", clientId);
//        map.add("client_secret", clientSecret);

        map.add("username", username);
        map.add("password", plainPassword);
        map.add("scope", "openid profile");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        var responseEntity = oauthServerRestTemplate
                .postForEntity("/realms/katarinazart/protocol/openid-connect/token", requestEntity, OAuthResponse.class);

        //then
        log.debug("Response from OAuth2.0 server: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        OAuthResponse oAuthResponse = responseEntity.getBody();
        assertThat(oAuthResponse)
                .hasNoNullFieldsOrProperties();

        String token = oAuthResponse.getAccessToken();
        log.debug("JWT Access Token is {}", token);
        return token;
    }

    protected User createNewUser() {
        var account = Account.builder()
                .username(FAKER.name().username())
                .password(FAKER.regexify("[a-z]{6}[1-9]{6}[A-Z]{6}[!@#&()]{2}"))
                .roles(List.of(Role.READ_PRIVILEGE))
                .build();

        return User.builder()
                .id(UUID.randomUUID().toString())
                .firstname(FAKER.name().firstName())
                .lastname(FAKER.name().lastName())
                .emailAddress(FAKER.bothify("????##@gmail.com"))
                .account(account)
                .build();
    }

    protected UserCreateDto createNewUserDto() {
        var accountDto = AccountDto.builder()
                .username(FAKER.name().username())
                .password(FAKER.regexify("[a-z]{6}[1-9]{6}[A-Z]{6}[!@#&()]{2}"))
                .roles(List.of(Role.READ_PRIVILEGE))
                .build();

        return UserCreateDto.builder()
                .firstname(FAKER.name().firstName())
                .lastname(FAKER.name().lastName())
                .emailAddress(FAKER.bothify("????##@gmail.com"))
                .account(accountDto)
                .build();
    }
}