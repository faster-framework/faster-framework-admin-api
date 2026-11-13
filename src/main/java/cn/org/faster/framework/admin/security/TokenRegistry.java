package cn.org.faster.framework.admin.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 令牌登记表：负责单端登录判定与登出/改密失效。
 *
 * <p>实现为进程内存储，适用于单实例部署。多实例部署请替换为 Redis 实现
 * （保持本类方法签名即可）。</p>
 *
 * @author zhangbowen
 */
@Component
public class TokenRegistry {

    private static final int CLEANUP_THRESHOLD = 1024;

    private final Clock clock;

    /**
     * userId -> 当前有效令牌 jti。
     */
    private final Map<Long, String> currentTokens = new ConcurrentHashMap<>();

    /**
     * userId -> 该时刻之前签发的令牌全部失效（改密/重置密码时写入）。
     */
    private final Map<Long, Instant> revokedBefore = new ConcurrentHashMap<>();

    /**
     * 已主动登出的 jti -> 令牌过期时间。
     */
    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public TokenRegistry(Clock clock) {
        this.clock = clock;
    }

    /**
     * 登记新签发的令牌。
     *
     * @param userId 用户 id
     * @param tokenId 令牌 id
     */
    public void register(long userId, String tokenId) {
        cleanupIfNecessary();
        currentTokens.put(userId, tokenId);
    }

    /**
     * @param userId  用户 id
     * @param tokenId 令牌 id
     * @return 是否为该用户当前唯一的令牌
     */
    public boolean isCurrent(long userId, String tokenId) {
        return tokenId != null && tokenId.equals(currentTokens.get(userId));
    }

    /**
     * @param tokenId 令牌 id
     * @return 是否已登出
     */
    public boolean isRevoked(String tokenId) {
        Instant expiresAt = revokedTokens.get(tokenId);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(clock.instant())) {
            revokedTokens.remove(tokenId);
            return false;
        }
        return true;
    }

    /**
     * @param userId    用户 id
     * @param issuedAt  令牌签发时间
     * @return 令牌是否签发于“全量失效”时刻之前
     */
    public boolean isIssuedBefore(long userId, Instant issuedAt) {
        if (issuedAt == null) {
            return false;
        }
        Instant revokedBeforeInstant = revokedBefore.get(userId);
        return revokedBeforeInstant != null && issuedAt.isBefore(revokedBeforeInstant);
    }

    /**
     * 登出：撤销当前令牌。
     *
     * @param userId    用户 id
     * @param tokenId   令牌 id
     * @param expiresAt 令牌过期时间
     */
    public void revoke(long userId, String tokenId, Instant expiresAt) {
        currentTokens.remove(userId, tokenId);
        if (tokenId != null) {
            revokedTokens.put(tokenId, expiresAt == null ? clock.instant().plusSeconds(7200) : expiresAt);
        }
    }

    /**
     * 使该用户所有已签发令牌失效（修改/重置密码、删除用户时调用）。
     *
     * @param userId 用户 id
     */
    public void revokeAll(long userId) {
        currentTokens.remove(userId);
        revokedBefore.put(userId, clock.instant());
    }

    private void cleanupIfNecessary() {
        if (revokedTokens.size() < CLEANUP_THRESHOLD) {
            return;
        }
        Instant now = clock.instant();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
