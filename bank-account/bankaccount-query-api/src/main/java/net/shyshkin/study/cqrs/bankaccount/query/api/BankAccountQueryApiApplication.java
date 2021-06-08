package net.shyshkin.study.cqrs.bankaccount.query.api;

import net.shyshkin.study.cqrs.bankaccount.core.config.AxonConfig;
import net.shyshkin.study.cqrs.bankaccount.core.models.BankAccount;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AxonConfig.class})
@EntityScan(basePackageClasses = BankAccount.class)
public class BankAccountQueryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankAccountQueryApiApplication.class, args);
    }

}
