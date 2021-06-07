package net.shyshkin.study.cqrs.user.cmd.api.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.cmd.api.commontest.AbstractDockerComposeTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ResourceServerConfigTest extends AbstractDockerComposeTest {

    @Test
    @DisplayName("Anonymous user should have access to the Health Endpoint")
    void getHealthEndpoint_anonymousUser() {

        //when
        ResponseEntity<HealthResponse> responseEntity = restTemplate.getForEntity("/actuator/health", HealthResponse.class);

        //then
        log.debug("Response entity: {}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        var healthResponse = responseEntity.getBody();
        assertThat(healthResponse)
                .isNotNull()
                .hasFieldOrPropertyWithValue("status", "UP");
    }

    @Data
    static class HealthResponse {
        private String status;
    }
}