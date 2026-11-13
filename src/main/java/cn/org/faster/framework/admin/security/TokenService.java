package cn.org.faster.framework.admin.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * 访问令牌签发。
 *
 * <p>使用 Spring Security 7 的 {@link JwtEncoder}（Nimbus JOSE，HS256），
 * 令牌包含 {@code iss/sub/iat/exp/jti}，其中 {@code jti} 用于登出撤销与单端登录判定。</p>
 *
 * @author zhangbowen
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties properties;

    private final TokenRegistry tokenRegistry;

    private final Clock clock;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties properties,
                        TokenRegistry tokenRegistry, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.tokenRegistry = tokenRegistry;
        this.clock = clock;
    }

    /**
     * 为用户签发令牌。
     *
     * @param userId 用户 id
     * @param name   用户姓名（写入自定义 claim，便于前端直接展示）
     * @return 已签发的令牌
     */
    public IssuedToken issue(long userId, String name) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.ttl());
        String tokenId = UUID.randomUUID().toString();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(Long.toString(userId))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(tokenId)
                .claim("name", name)
                .build();
        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims));
        tokenRegistry.register(userId, tokenId);
        return new IssuedToken(jwt.getTokenValue(), tokenId, expiresAt);
    }
}
