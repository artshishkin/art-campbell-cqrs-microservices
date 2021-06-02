package net.shyshkin.study.cqrs.user.cmd.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEncoderImpl implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
