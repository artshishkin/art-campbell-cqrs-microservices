package net.shyshkin.study.cqrs.user.core.converters;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class KeycloakAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(getRoleBasedAuthorities(jwt));
        authorities.addAll(getScopeBasedAuthorities(jwt));
        log.debug("Convert to granted authorities: {}", authorities);
        return authorities;
    }

    private List<GrantedAuthority> getRoleBasedAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        return Optional
                .ofNullable(realmAccess)
                .map(rAccess -> rAccess.get("roles"))
                .filter(roles -> roles instanceof List)
                .map(roles -> (List<String>) roles)
                .stream()
                .flatMap(Collection::stream)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private List<GrantedAuthority> getScopeBasedAuthorities(Jwt jwt) {
        String scopeField = jwt.getClaimAsString("scope");
        return Optional
                .ofNullable(scopeField)
                .filter(Strings::isNotBlank)
                .map(scopes -> scopes.split(" "))
                .stream()
                .flatMap(Stream::of)
                .filter(Strings::isNotBlank)
                .map("SCOPE_"::concat)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }
}
