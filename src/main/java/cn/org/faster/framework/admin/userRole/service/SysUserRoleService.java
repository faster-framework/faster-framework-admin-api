package cn.org.faster.framework.admin.userRole.service;

import cn.org.faster.framework.admin.common.util.AuditSupport;
import cn.org.faster.framework.admin.permission.service.PermissionCache;
import cn.org.faster.framework.admin.userRole.entity.SysUserRole;
import cn.org.faster.framework.admin.userRole.repository.SysUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户-角色关系服务。
 *
 * @author zhangbowen
 */
@Service
@Transactional
public class SysUserRoleService {

    private final SysUserRoleRepository userRoleRepository;

    private final PermissionCache permissionCache;

    private final AuditSupport auditSupport;

    private final Clock clock;

    public SysUserRoleService(SysUserRoleRepository userRoleRepository, PermissionCache permissionCache,
                              AuditSupport auditSupport, Clock clock) {
        this.userRoleRepository = userRoleRepository;
        this.permissionCache = permissionCache;
        this.auditSupport = auditSupport;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Long> roles(long userId) {
        return userRoleRepository.findRoleIds(userId);
    }

    /**
     * 全量覆盖用户角色。
     *
     * @param userId 用户 id
     * @param list   角色关系列表
     */
    public void batchChoose(long userId, List<SysUserRole> list) {
        List<Long> roleIds = list == null ? List.of()
                : list.stream().map(SysUserRole::getRoleId).filter(Objects::nonNull).distinct().toList();
        LocalDateTime now = LocalDateTime.now(clock);
        userRoleRepository.replace(userId, roleIds, auditSupport.currentUserId(), now);
        permissionCache.evict(userId);
    }

    public void deleteByUserId(long userId) {
        userRoleRepository.deleteByUserId(userId);
        permissionCache.evict(userId);
    }

    public void deleteByRoleId(long roleId) {
        userRoleRepository.deleteByRoleId(roleId);
        permissionCache.clear();
    }
}
