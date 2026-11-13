package cn.org.faster.framework.admin.permission.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 权限缓存配置。
 *
 * @param ttl 缓存有效期；角色/权限变更时会主动清空缓存
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.permission-cache")
public record PermissionCacheProperties(Duration ttl) {

    public PermissionCacheProperties {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofMinutes(5);
        }
    }
}
