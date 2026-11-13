package cn.org.faster.framework.admin.security;

import cn.org.faster.framework.admin.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 令牌登记表单元测试。
 *
 * @author zhangbowen
 */
class TokenRegistryTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));

    private final TokenRegistry registry = new TokenRegistry(clock);

    @Test
    void tracksCurrentTokenPerUser() {
        registry.register(1L, "jti-1");

        assertThat(registry.isCurrent(1L, "jti-1")).isTrue();
        assertThat(registry.isCurrent(1L, "jti-2")).isFalse();
        assertThat(registry.isCurrent(2L, "jti-1")).isFalse();
    }

    @Test
    void revokedTokenStaysInvalidUntilExpiry() {
        registry.revoke(1L, "jti-1", clock.instant().plusSeconds(600));

        assertThat(registry.isRevoked("jti-1")).isTrue();

        clock.advance(Duration.ofSeconds(601));
        assertThat(registry.isRevoked("jti-1")).isFalse();
    }

    @Test
    void revokeAllInvalidatesPreviouslyIssuedTokens() {
        Instant issuedAt = clock.instant();
        clock.advance(Duration.ofMinutes(5));
        registry.revokeAll(1L);

        assertThat(registry.isIssuedBefore(1L, issuedAt)).isTrue();
        assertThat(registry.isIssuedBefore(1L, clock.instant().plusSeconds(1))).isFalse();
        assertThat(registry.isIssuedBefore(2L, issuedAt)).isFalse();
    }
}
