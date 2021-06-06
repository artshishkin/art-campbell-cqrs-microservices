package net.shyshkin.study.cqrs.user.oauth2_0.testcontainers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

@Slf4j
@Getter
public class TestComposeContainer extends DockerComposeContainer<TestComposeContainer> {

    private static final String COMPOSE_FILE_PATH = "src/test/resources/compose-test.yml";
    private static TestComposeContainer container;

    private static boolean containerStarted = false;

    public TestComposeContainer() {
        super(new File(COMPOSE_FILE_PATH));
    }

    public static TestComposeContainer getInstance() {
        if (container == null) {
            container = new TestComposeContainer()
                    .withExposedService("mongo_1", 27017)
            ;
        }
        return container;
    }

    @Override
    public void start() {

        if (!containerStarted) super.start();

        containerStarted = true;

        String mongodbHost = container.getServiceHost("mongo_1", 27017);
        Integer mongodbPort = container.getServicePort("mongo_1", 27017);

        System.setProperty("MONGODB_HOST", mongodbHost);
        System.setProperty("MONGODB_PORT", String.valueOf(mongodbPort));

        log.debug("MongoDB: {}:{}", mongodbHost, mongodbPort);

    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
