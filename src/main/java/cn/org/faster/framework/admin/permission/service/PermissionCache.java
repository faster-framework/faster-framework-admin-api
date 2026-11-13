package cn.org.faster.framework.admin.permission.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户权限缓存。
 *
 * <p>缓存的 value 是权限编码集合；任何角色/权限/用户角色变更都会主动
 * {@link #clear()}，同时设置 TTL 作为兜底。</p>
 *
 * @author zhangbowen
 */
@Component
public class PermissionCache {

    private record Entry(Set<String> codes, Instant expiresAt) {
    }

    private final Map<Long, Entry> cache = new ConcurrentHashMap<>();

    private final Clock clock;

    private final PermissionCacheProperties properties;

    public PermissionCache(Clock clock, PermissionCacheProperties properties) {
        this.clock = clock;
        this.properties = properties;
    }

    public Optional<Set<String>> get(long userId) {
        Entry entry = cache.get(userId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(clock.instant())) {
            cache.remove(userId, entry);
            return Optional.empty();
        }
        return Optional.of(entry.codes());
    }

    public void put(long userId, Set<String> codes) {
        cache.put(userId, new Entry(Set.copyOf(codes), clock.instant().plus(properties.ttl())));
    }

    public void evict(long userId) {
        cache.remove(userId);
    }

    public void clear() {
        cache.clear();
    }
}
