package net.shyshkin.study.cqrs.user.oauth2_0;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@SpringBootApplication
@EnableAuthorizationServer
public class UserOauth20Application {

    public static void main(String[] args) {
        SpringApplication.run(UserOauth20Application.class, args);
    }

}
