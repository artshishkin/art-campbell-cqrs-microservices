package net.shyshkin.study.cqrs.user.oauth2_0.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.models.Account;
import net.shyshkin.study.cqrs.user.core.models.Role;
import net.shyshkin.study.cqrs.user.core.models.User;
import net.shyshkin.study.cqrs.user.oauth2_0.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitUsers implements CommandLineRunner {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (repository.count() == 0) {
            initDB();
        }
    }

    private void initDB() {
        User art = User.builder()
                .id(UUID.randomUUID().toString())
                .firstname("Art")
                .lastname("Shyshkin")
                .emailAddress("d.art.shishkin@gmail.com")
                .account(
                        Account.builder()
                                .username("shyshkin.art")
                                .password(passwordEncoder.encode("P@ssW0rd!"))
                                .roles(List.of(Role.WRITE_PRIVILEGE, Role.READ_PRIVILEGE))
                                .build()
                )
                .build();

        User kate = User.builder()
                .id(UUID.randomUUID().toString())
                .firstname("Kate")
                .lastname("Shyshkina")
                .emailAddress("kate.shishkina@gmail.com")
                .account(
                        Account.builder()
                                .username("shyshkina.kate")
                                .password(passwordEncoder.encode("P@ssW0rd1"))
                                .roles(List.of(Role.READ_PRIVILEGE))
                                .build()
                )
                .build();

        repository.save(art);
        repository.save(kate);
        log.debug("DB Initialization finished successfully");
    }

}
