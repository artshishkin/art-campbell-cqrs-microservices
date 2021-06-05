package net.shyshkin.study.cqrs.user.cmd.api.commontest;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.testcontainers.TestComposeContainer;
import net.shyshkin.study.cqrs.user.core.dto.OAuthResponse;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "axon.axonserver.servers=${AXON_SERVERS}",
        "spring.data.mongodb.host=${MONGODB_HOST}",
        "spring.data.mongodb.port=${MONGODB_PORT}"

})
@Testcontainers
public abstract class AbstractDockerComposeTest {

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
    protected static String clientSecret = "springbankSecret";

    RestTemplate oauthServerRestTemplate;

    protected void getJwtAccessToken(String username, String plainPassword) {
        oauthServerRestTemplate = restTemplateBuilder
                .basicAuthentication(clientId, clientSecret)
                .rootUri(String.format("http://%s:%d", composeContainer.getOauthHost(), composeContainer.getOauthPort()))
                .build();

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");

        map.add("username", username);
        map.add("password", plainPassword);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        var responseEntity = oauthServerRestTemplate
                .postForEntity("/oauth/token", requestEntity, OAuthResponse.class);

        //then
        log.debug("Response from OAuth2.0 server: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        OAuthResponse oAuthResponse = responseEntity.getBody();
        assertThat(oAuthResponse)
                .hasNoNullFieldsOrProperties();

        jwtAccessToken = oAuthResponse.getAccessToken();
        log.debug("JWT Access Token is {}", jwtAccessToken);
    }


}