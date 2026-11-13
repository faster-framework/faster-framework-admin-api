package cn.org.faster.framework.admin.security;

import cn.org.faster.framework.admin.support.MutableClock;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 令牌业务校验单元测试。
 *
 * @author zhangbowen
 */
class TokenValidatorTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));

    private final TokenRegistry registry = new TokenRegistry(clock);

    @Test
    void acceptsCurrentTokenInSingleSessionMode() {
        TokenValidator validator = validator(false);
        registry.register(1L, "jti-1");

        assertThat(validator.validate(jwt("jti-1", clock.instant())).hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenReplacedByNewerLoginInSingleSessionMode() {
        TokenValidator validator = validator(false);
        registry.register(1L, "jti-1");
        registry.register(1L, "jti-2");

        assertThat(validator.validate(jwt("jti-1", clock.instant())).hasErrors()).isTrue();
    }

    @Test
    void acceptsMultipleSessionsWhenAllowed() {
        TokenValidator validator = validator(true);
        registry.register(1L, "jti-2");

        assertThat(validator.validate(jwt("jti-1", clock.instant())).hasErrors()).isFalse();
    }

    @Test
    void rejectsRevokedToken() {
        TokenValidator validator = validator(true);
        registry.revoke(1L, "jti-1", clock.instant().plusSeconds(600));

        assertThat(validator.validate(jwt("jti-1", clock.instant())).hasErrors()).isTrue();
    }

    @Test
    void rejectsTokenIssuedBeforePasswordChange() {
        TokenValidator validator = validator(true);
        Instant issuedAt = clock.instant();
        clock.advance(Duration.ofMinutes(1));
        registry.revokeAll(1L);

        assertThat(validator.validate(jwt("jti-2", issuedAt)).hasErrors()).isTrue();
        assertThat(validator.validate(jwt("jti-3", clock.instant())).hasErrors()).isFalse();
    }

    private TokenValidator validator(boolean multipartTerminal) {
        return new TokenValidator(registry, new AuthProperties(multipartTerminal, "123456"));
    }

    private Jwt jwt(String jti, Instant issuedAt) {
        return Jwt.withTokenValue("token-value")
                .header("alg", "HS256")
                .subject("1")
                .jti(jti)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(600))
                .build();
    }
}
