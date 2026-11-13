package cn.org.faster.framework.admin.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 轻量 HMAC-SHA256 签名工具，用于上传凭证等短时令牌。
 *
 * @author zhangbowen
 */
public final class HmacSigner {

    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] key;

    public HmacSigner(byte[] key) {
        this.key = key.clone();
    }

    /**
     * @param payload 待签名内容
     * @return Base64URL（无填充）签名
     */
    public String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC 签名失败", ex);
        }
    }

    /**
     * 恒定时间比较签名。
     *
     * @param payload   原始内容
     * @param signature 待校验签名
     * @return 是否合法
     */
    public boolean verify(String payload, String signature) {
        if (payload == null || signature == null) {
            return false;
        }
        byte[] expected = sign(payload).getBytes(StandardCharsets.UTF_8);
        byte[] actual = signature.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
