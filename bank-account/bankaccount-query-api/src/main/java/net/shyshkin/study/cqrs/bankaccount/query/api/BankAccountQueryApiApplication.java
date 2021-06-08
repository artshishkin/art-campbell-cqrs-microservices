package net.shyshkin.study.cqrs.bankaccount.query.api;

import net.shyshkin.study.cqrs.bankaccount.core.config.AxonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AxonConfig.class})
public class BankAccountQueryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankAccountQueryApiApplication.class, args);
    }

}
