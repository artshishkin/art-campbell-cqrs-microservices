package net.shyshkin.study.cqrs.user.core.converters;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class KeycloakAuthorityConverterTest {

    static KeycloakAuthorityConverter converter;

    @BeforeAll
    static void beforeAll() {
        converter = new KeycloakAuthorityConverter();
    }

    @Test
    void convert_presentRoles() {
        //given
        List<String> roles = List.of(
                "default-roles-katarinazart",
                "offline_access",
                "developer",
                "uma_authorization"
        );
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("realm_access", Map.of("roles", roles))
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .hasSize(4)
                .allSatisfy(grantedAuthority -> assertThat(grantedAuthority.getAuthority()).isIn(roles));

        log.debug("Authorities: {}", authorities);

    }

    @Test
    void convert_absentRealmAccessClaim() {
        //given
        List<String> roles = List.of(
                "default-roles-katarinazart",
                "offline_access",
                "developer",
                "uma_authorization"
        );
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("fake_realm_access", Map.of("roles", roles))
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .isEmpty();

        log.debug("Authorities: {}", authorities);

    }

    @Test
    void convert_absentRoles() {
        //given
        List<String> roles = List.of(
                "default-roles-katarinazart",
                "offline_access",
                "developer",
                "uma_authorization"
        );
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("realm_access", Map.of("fake_roles", roles))
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .isEmpty();

        log.debug("Authorities: {}", authorities);

    }

    @Test
    void convert_rolesEmpty() {
        //given
        List<String> roles = List.of();
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("realm_access", Map.of("roles", roles))
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .isEmpty();

        log.debug("Authorities: {}", authorities);

    }

    @Test
    void convert_presentScope() {
        //given
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("scope", "openid profile email")
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .hasSize(3)
                .allSatisfy(grantedAuthority -> assertThat(grantedAuthority.getAuthority()).startsWith("SCOPE_"));

        log.debug("Authorities: {}", authorities);
    }

    @Test
    void convert_emptyScope() {
        //given
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("scope", "")
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .isEmpty();

        log.debug("Authorities: {}", authorities);
    }

    @Test
    void convert_absentScope() {
        //given
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("no_scope", "foo buzz bar")
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .isEmpty();

        log.debug("Authorities: {}", authorities);
    }

    @Test
    void convert_manySpacesInScopeField() {
        //given
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("scope", "   openid         profile    ")
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .hasSize(2)
                .allSatisfy(grantedAuthority -> assertThat(grantedAuthority.getAuthority()).startsWith("SCOPE_"));

        log.debug("Authorities: {}", authorities);
    }

    @Test
    void convert_presentScopeAndRoles() {
        //given
        List<String> roles = List.of(
                "default-roles-katarinazart",
                "offline_access",
                "developer",
                "uma_authorization"
        );
        Jwt jwt = Jwt.withTokenValue("eyJhbG___SOME_HUGE_TOKEN___6o0DA")
                .header("foo", "buzz")
                .claim("realm_access", Map.of("roles", roles))
                .claim("scope", "openid profile")
                .build();
        //when
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        //then
        assertThat(authorities)
                .hasSize(6)
                .allSatisfy(grantedAuthority -> assertThat(grantedAuthority.getAuthority())
                        .satisfiesAnyOf(
                                authority -> assertThat(authority).isIn(roles),
                                authority -> assertThat(authority).startsWith("SCOPE_")
                        )
                );

        log.debug("Authorities: {}", authorities);
    }
}