package net.shyshkin.study.cqrs.apigateway.testcontainers;

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
                    .waitingFor("axon-server_1",
                            Wait.forLogMessage(".*Started AxonServer in.*\\n", 1))
                    .waitingFor("mongo_1", Wait.forHealthcheck())
                    .withExposedService("user-cmd-api_1", 8080, Wait.forHealthcheck())
                    .withExposedService("user-query-api_1", 8080, Wait.forHealthcheck())
                    .withExposedService("oauth20-server_1", 8080,
                            Wait.forLogMessage(".*Admin console listening on.*\\n", 1))
                    .withExposedService("bankaccount-cmd-api_1", 8080, Wait.forHealthcheck())
                    .withExposedService("bankaccount-query-api_1", 8080, Wait.forHealthcheck())
                    .waitingFor("mysql_1", Wait.forHealthcheck())
            ;
        }
        return container;
    }

    @Override
    public void start() {

        if (!containerStarted) super.start();

        containerStarted = true;

        oauthHost = container.getServiceHost("oauth20-server_1", 8080);
        oauthPort = container.getServicePort("oauth20-server_1", 8080);
        System.setProperty("OAUTH_URI", String.format("http://%s:%d", oauthHost, oauthPort));
        log.debug("oauth20-server: {}:{}", oauthHost, oauthPort);

        setServiceUriSystemProperty("user-cmd-api", 8080);
        setServiceUriSystemProperty("user-query-api", 8080);
        setServiceUriSystemProperty("bankaccount-cmd-api", 8080);
        setServiceUriSystemProperty("bankaccount-query-api", 8080);
    }

    private void setServiceUriSystemProperty(String serviceName, int exposedPort) {

        String dockerInstanceName = serviceName + "_1";

        String host = container.getServiceHost(dockerInstanceName, exposedPort);
        Integer port = container.getServicePort(dockerInstanceName, exposedPort);

        log.debug("{}: {}:{}", serviceName, host, port);

        String PROPERTY_NAME = serviceName.toUpperCase()
                .replace("-", "_")
                .replace(".", "_");
        PROPERTY_NAME += "_URI";
        System.setProperty(PROPERTY_NAME, String.format("http://%s:%d", host, port));
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
