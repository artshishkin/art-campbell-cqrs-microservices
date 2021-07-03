package net.shyshkin.study.cqrs.user.query.api.config;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.cqrs.user.core.converters.KeycloakAuthorityConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.net.InetAddress;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        String allowedIpAddresses = String.format("hasIpAddress('%s/16') or hasIpAddress('127.0.0.1')", hostAddress);
        log.debug("Host IP Address: {}, allowed IP addresses: {}", hostAddress, allowedIpAddresses);

        http.authorizeRequests()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/api/v1/users/provider/**")
                .access(allowedIpAddresses)
//                .permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable();

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakAuthorityConverter());

        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter);
    }
}
