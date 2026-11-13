package cn.org.faster.framework.admin.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * JWT 配置。
 *
 * @param secret    Base64 编码的 HMAC 密钥（生产环境必须通过环境变量注入，长度至少 32 字节）
 * @param issuer    签发者，校验时会比对
 * @param ttl       令牌有效期
 * @param clockSkew 允许的时钟偏移
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, String issuer, Duration ttl, Duration clockSkew) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "faster-admin";
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofHours(2);
        }
        if (clockSkew == null || clockSkew.isNegative()) {
            clockSkew = Duration.ofSeconds(30);
        }
    }

    /**
     * @return HS256 密钥
     */
    public SecretKey secretKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("必须配置 app.jwt.secret");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret 解码后至少需要 32 字节（256 位）");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
