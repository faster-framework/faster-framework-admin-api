package cn.org.faster.framework.admin.permission.service;

import cn.org.faster.framework.admin.permission.repository.SysPermissionRepository;
import cn.org.faster.framework.admin.userRole.repository.SysUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限解析服务：计算某个用户拥有的全部权限编码。
 *
 * <p>规则与旧版一致：拥有角色 0（超级管理员）的用户自动拥有全部权限，
 * 其余用户通过角色-权限关联表计算。</p>
 *
 * @author zhangbowen
 */
@Service
public class PermissionService {

    /**
     * 超级管理员角色 id。
     */
    public static final long ADMIN_ROLE_ID = 0L;

    private final SysUserRoleRepository userRoleRepository;

    private final SysPermissionRepository permissionRepository;

    private final PermissionCache permissionCache;

    public PermissionService(SysUserRoleRepository userRoleRepository,
                             SysPermissionRepository permissionRepository,
                             PermissionCache permissionCache) {
        this.userRoleRepository = userRoleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionCache = permissionCache;
    }

    /**
     * @param userId 用户 id
     * @return 权限编码集合（不可变）
     */
    @Transactional(readOnly = true)
    public Set<String> codesOf(long userId) {
        return permissionCache.get(userId).orElseGet(() -> {
            Set<String> codes = load(userId);
            permissionCache.put(userId, codes);
            return codes;
        });
    }

    /**
     * 清空全部用户权限缓存（角色、权限、用户角色发生变更时调用）。
     */
    public void evictAll() {
        permissionCache.clear();
    }

    /**
     * 清理单个用户的权限缓存。
     *
     * @param userId 用户 id
     */
    public void evict(long userId) {
        permissionCache.evict(userId);
    }

    private Set<String> load(long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIds(userId);
        if (roleIds.contains(ADMIN_ROLE_ID)) {
            return Set.copyOf(new LinkedHashSet<>(permissionRepository.findAllCodes()));
        }
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(new LinkedHashSet<>(permissionRepository.findCodesByRoleIds(roleIds)));
    }
}
