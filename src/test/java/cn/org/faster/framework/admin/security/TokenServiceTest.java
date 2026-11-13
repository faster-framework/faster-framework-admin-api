package cn.org.faster.framework.admin.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 令牌签发与解析单元测试（真实 Nimbus 编解码器）。
 *
 * @author zhangbowen
 */
class TokenServiceTest {

    private final Clock clock = Clock.systemUTC();

    private final SecretKey key = new SecretKeySpec(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtProperties properties = new JwtProperties(
            Base64.getEncoder().encodeToString(key.getEncoded()),
            "faster-admin", Duration.ofHours(2), Duration.ofSeconds(30));

    private final TokenRegistry registry = new TokenRegistry(clock);

    @Test
    void issuesSignedTokenWithClaimsAndExpiry() {
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        TokenService tokenService = new TokenService(encoder, properties, registry, clock);

        IssuedToken issued = tokenService.issue(5L, "张三");
        Jwt jwt = decoder.decode(issued.value());

        assertThat(jwt.getSubject()).isEqualTo("5");
        assertThat(jwt.getId()).isEqualTo(issued.id());
        assertThat(jwt.getClaimAsString("name")).isEqualTo("张三");
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(clock.instant());
        assertThat(registry.isCurrent(5L, issued.id())).isTrue();
    }

    @Test
    void rejectsTokenSignedByAnotherKey() {
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        SecretKey otherKey = new SecretKeySpec(
                "ffffffffffffffffffffffffffffffff".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        TokenService tokenService = new TokenService(
                NimbusJwtEncoder.withSecretKey(otherKey).algorithm(MacAlgorithm.HS256).build(),
                properties, registry, clock);

        String token = tokenService.issue(6L, "李四").value();

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> decoder.decode(token)))
                .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
    }
}
