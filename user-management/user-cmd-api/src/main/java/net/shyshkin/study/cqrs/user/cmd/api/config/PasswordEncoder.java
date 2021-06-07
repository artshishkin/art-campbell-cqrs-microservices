package net.shyshkin.study.cqrs.user.cmd.api.config;

public interface PasswordEncoder {
    String hashPassword(String password);
}
