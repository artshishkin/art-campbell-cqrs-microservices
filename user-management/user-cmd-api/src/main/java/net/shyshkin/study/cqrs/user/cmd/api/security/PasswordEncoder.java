package net.shyshkin.study.cqrs.user.cmd.api.security;

public interface PasswordEncoder {
    String hashPassword(String password);
}
