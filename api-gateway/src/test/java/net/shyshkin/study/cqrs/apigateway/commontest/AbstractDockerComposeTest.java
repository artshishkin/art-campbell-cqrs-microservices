package net.shyshkin.study.cqrs.apigateway.commontest;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.apigateway.dto.OAuthResponse;
import net.shyshkin.study.cqrs.apigateway.testcontainers.TestComposeContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.routes.uri.user-cmd-api=${USER_CMD_API_URI}",
        "app.routes.uri.user-query-api=${USER_QUERY_API_URI}",
        "app.routes.uri.bankaccount-cmd-api=${BANKACCOUNT_CMD_API_URI}"
})
@Testcontainers
public abstract class AbstractDockerComposeTest {

    protected static final Faker FAKER = Faker.instance(new Locale("en-GB"));

    protected WebTestClient webTestClient;

    protected static String jwtAccessToken;

    @Container
    public static TestComposeContainer composeContainer = TestComposeContainer.getInstance();

    @LocalServerPort
    protected int randomServerPort;

    protected WebClient oauthServerWebClient;

    private static final String CLIENT_ID = "springbankClient";
    private static final String CLIENT_SECRET = "springbankSecret";

    protected String getJwtAccessToken(String username, String plainPassword) {

        oauthServerWebClient = WebClient
                .builder()
                .defaultHeaders(headers -> headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET))
                .baseUrl(String.format("http://%s:%d", composeContainer.getOauthHost(), composeContainer.getOauthPort()))
                .build();


        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");

        map.add("username", username);
        map.add("password", plainPassword);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        ResponseEntity<OAuthResponse> responseEntity = oauthServerWebClient
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .toEntity(OAuthResponse.class)
                .doOnNext(entity -> log.debug("Response from OAuth2.0 server: {}", entity))
                .block();

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var oAuthResponse = responseEntity.getBody();

        assertThat(oAuthResponse)
                .hasNoNullFieldsOrProperties();

        String token = oAuthResponse.getAccessToken();
        log.debug("JWT Access Token is {}", token);
        return token;
    }

}