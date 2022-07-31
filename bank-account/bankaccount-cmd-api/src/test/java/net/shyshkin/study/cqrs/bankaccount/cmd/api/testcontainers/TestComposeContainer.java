package net.shyshkin.study.cqrs.bankaccount.cmd.api.testcontainers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Slf4j
@Getter
public class TestComposeContainer extends DockerComposeContainer<TestComposeContainer> {

    private static final String COMPOSE_FILE_PATH = "src/test/resources/compose-test.yml";
    private static TestComposeContainer container;

    private static boolean containerStarted = false;

    private String oauthHost;
    private Integer oauthPort;

    public TestComposeContainer() {
        super(new File(COMPOSE_FILE_PATH));
    }

    public static TestComposeContainer getInstance() {
        if (container == null) {
            container = new TestComposeContainer()
                    .withLocalCompose(true)
                    .withOptions("--compatibility")
                    .withTailChildContainers(true)
                    .withExposedService("axon-server_1", 8124,
                            Wait.forLogMessage(".*Started AxonServer in.*\\n", 1))
                    .withExposedService("mongo_1", 27017)
                    .withExposedService("oauth20-server_1", 8080,
                            Wait.forLogMessage(".*Admin console listening on.*\\n", 1))
                    .waitingFor("user-query-api_1", Wait.forHealthcheck())
            ;
        }
        return container;
    }

    @Override
    public void start() {

        if (!containerStarted) super.start();

        containerStarted = true;

        String axonHost = container.getServiceHost("axon-server_1", 8124);
        Integer axonPort = container.getServicePort("axon-server_1", 8124);
        String servers = axonHost + ":" + axonPort;
        System.setProperty("AXON_SERVERS", servers);

        log.debug("AXON_SERVERS: {}", servers);

        String mongodbHost = container.getServiceHost("mongo_1", 27017);
        Integer mongodbPort = container.getServicePort("mongo_1", 27017);

        System.setProperty("MONGODB_HOST", mongodbHost);
        System.setProperty("MONGODB_PORT", String.valueOf(mongodbPort));

        log.debug("MongoDB: {}:{}", mongodbHost, mongodbPort);

        oauthHost = container.getServiceHost("oauth20-server_1", 8080);
        oauthPort = container.getServicePort("oauth20-server_1", 8080);
        System.setProperty("OAUTH_URI", String.format("http://%s:%d", oauthHost, oauthPort));
        log.debug("oauth20-server: {}:{}", oauthHost, oauthPort);

    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
