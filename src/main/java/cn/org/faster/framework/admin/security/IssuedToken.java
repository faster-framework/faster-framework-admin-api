package cn.org.faster.framework.admin.security;

import java.time.Instant;

/**
 * 已签发的令牌。
 *
 * @param value     令牌字符串
 * @param id        令牌 id（jti）
 * @param expiresAt 过期时间
 * @author zhangbowen
 */
public record IssuedToken(String value, String id, Instant expiresAt) {
}
