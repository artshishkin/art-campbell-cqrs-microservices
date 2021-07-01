package net.shyshkin.study.cqrs.user.query.api.commontest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.dto.OAuthResponse;
import net.shyshkin.study.cqrs.user.query.api.testcontainers.TestComposeContainer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
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

import java.util.Locale;

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
    protected RestTemplateBuilder restTemplateBuilder;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected static String jwtAccessToken;

    @Container
    public static TestComposeContainer composeContainer = TestComposeContainer.getInstance();

    @Autowired
    protected CommandGateway commandGateway;

    @LocalServerPort
    protected int randomServerPort;

    protected RestTemplate oauthServerRestTemplate;

    private static final String CLIENT_ID = "springbankClient";
    private static final String CLIENT_SECRET = "674ae476-7591-4078-82e9-5eaea5e71cff";

    protected String getJwtAccessToken(String username, String plainPassword) {
        oauthServerRestTemplate = restTemplateBuilder
                .basicAuthentication(CLIENT_ID, CLIENT_SECRET)
                .rootUri(String.format("http://%s:%d", composeContainer.getOauthHost(), composeContainer.getOauthPort()))
                .build();

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");

        map.add("username", username);
        map.add("password", plainPassword);
        map.add("scope", "openid profile");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        var responseEntity = oauthServerRestTemplate
                .postForEntity("/auth/realms/katarinazart/protocol/openid-connect/token", requestEntity, OAuthResponse.class);

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
}