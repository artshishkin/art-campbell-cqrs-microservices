package net.shyshkin.study.cqrs.user.oauth2_0;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.oauth2_0.dto.OAuthResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("For manual test only -> start `/infrastructure/docker-compose.yml` first")
class UserOauth20ApplicationManualTest {

    @Autowired
    TestRestTemplate restTemplate;

    //Previously Registered User
    private String plainPassword = "P@ssW0rd!";
    private String usernameFromDB = "shyshkin.art";

    @Value("${app.security.oauth2.client.client-id}")
    private String clientId;

    @Value("${app.security.oauth2.client.client-secret}")
    private String clientSecret;

    @Test
    void getAccessTokenFromOAuth20Server() {

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", usernameFromDB);
        map.add("password", plainPassword);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        var responseEntity = restTemplate
                .withBasicAuth(clientId, clientSecret)
                .postForEntity("/oauth/token", requestEntity, OAuthResponse.class);

        //then
        log.debug("Response from OAuth2.0 server: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasNoNullFieldsOrProperties();
    }

}
