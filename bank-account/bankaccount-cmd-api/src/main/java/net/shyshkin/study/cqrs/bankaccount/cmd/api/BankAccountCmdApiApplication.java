package net.shyshkin.study.cqrs.bankaccount.cmd.api;

import net.shyshkin.study.cqrs.bankaccount.core.config.AxonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import({AxonConfig.class})
public class BankAccountCmdApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankAccountCmdApiApplication.class, args);
    }

}
