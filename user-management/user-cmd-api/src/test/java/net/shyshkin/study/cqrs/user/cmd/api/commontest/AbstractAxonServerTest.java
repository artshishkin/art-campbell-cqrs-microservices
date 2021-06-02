package net.shyshkin.study.cqrs.user.cmd.api.commontest;

import net.shyshkin.study.cqrs.user.cmd.api.testcontainers.TestComposeContainer;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "axon.axonserver.servers=${AXON_SERVERS}",
        "spring.data.mongodb.host=${MONGODB_HOST}",
        "spring.data.mongodb.port=${MONGODB_PORT}"

})
@Testcontainers
public abstract class AbstractAxonServerTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Container
    public static TestComposeContainer axonServer = TestComposeContainer.getInstance();

    @Autowired
    protected CommandGateway commandGateway;
}