package cn.org.faster.framework.admin.upload.service;

import cn.org.faster.framework.admin.common.security.HmacSigner;
import cn.org.faster.framework.admin.security.JwtProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

/**
 * 预上传凭证签发与校验。
 *
 * <p>凭证 = Base64URL(payload).HMAC(payload)，payload 含用途、用户、过期时间与随机串。
 * 使用独立派生密钥（JWT 密钥无法直接用于伪造上传凭证）。</p>
 *
 * @author zhangbowen
 */
@Component
public class UploadTokenService {

    private static final String PURPOSE = "upload";

    private final HmacSigner signer;

    private final UploadProperties properties;

    private final Clock clock;

    public UploadTokenService(JwtProperties jwtProperties, UploadProperties properties, Clock clock) {
        this.signer = new HmacSigner(deriveKey(jwtProperties.secretKey().getEncoded()));
        this.properties = properties;
        this.clock = clock;
    }

    private static byte[] deriveKey(byte[] jwtKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            digest.update(jwtKey);
            digest.update("|upload".getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("无法派生上传凭证密钥", ex);
        }
    }

    /**
     * 为用户签发预上传凭证。
     *
     * @param userId 用户 id
     * @return 凭证与过期时间
     */
    public UploadToken issue(long userId) {
        long expiresAt = clock.millis() + properties.tokenTtl().toMillis();
        String payload = PURPOSE + ":" + userId + ":" + expiresAt + ":" + UUID.randomUUID();
        String encoded = encode(payload);
        return new UploadToken(encoded + "." + signer.sign(encoded), expiresAt);
    }

    /**
     * 校验凭证。
     *
     * @param token  凭证
     * @param userId 当前用户
     * @return 是否有效
     */
    public boolean verify(String token, long userId) {
        if (token == null || token.isBlank()) {
            return false;
        }
        int dot = token.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String encoded = token.substring(0, dot);
        if (!signer.verify(encoded, token.substring(dot + 1))) {
            return false;
        }
        String[] parts = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8).split(":");
        if (parts.length != 4 || !PURPOSE.equals(parts[0])) {
            return false;
        }
        try {
            long tokenUserId = Long.parseLong(parts[1]);
            long expiresAt = Long.parseLong(parts[2]);
            return tokenUserId == userId && expiresAt > clock.millis();
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String encode(String payload) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
