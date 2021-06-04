package net.shyshkin.study.cqrs.user.oauth2_0;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.oauth2_0.commontest.AbstractDockerComposeTest;
import net.shyshkin.study.cqrs.user.oauth2_0.dto.OAuthResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UserOauth20ApplicationTest extends AbstractDockerComposeTest {

    @Value("${app.security.oauth2.client.client-id}")
    private String clientId;

    @Value("${app.security.oauth2.client.client-secret}")
    private String clientSecret;

    @ParameterizedTest
    @CsvSource({
            "shyshkin.art,P@ssW0rd!",
            "shyshkina.kate,P@ssW0rd1"
    })
    void getAccessTokenFromOAuth20Server(String username, String plainPassword) {

        //when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", username);
        map.add("password", plainPassword);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        var responseEntity = restTemplate
                .withBasicAuth(clientId, clientSecret)
                .postForEntity("/oauth/token", requestEntity, OAuthResponse.class);

        //then
        log.debug("Response from OAuth2.0 server: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody())
                .hasNoNullFieldsOrProperties();
    }

}
