package net.shyshkin.study.cqrs.user.cmd.api;

import net.shyshkin.study.cqrs.user.core.configuration.AxonConfig;
import net.shyshkin.study.cqrs.user.core.configuration.I18nConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AxonConfig.class, I18nConfig.class})
public class UserCmdApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCmdApiApplication.class, args);
    }

}
