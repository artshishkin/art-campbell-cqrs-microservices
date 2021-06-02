package net.shyshkin.study.cqrs.user.query.api;

import net.shyshkin.study.cqrs.user.core.configuration.AxonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AxonConfig.class})
public class UserQueryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserQueryApiApplication.class, args);
    }

}
