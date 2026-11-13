package cn.org.faster.framework.admin.rolePermission.service;

import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.common.util.AuditSupport;
import cn.org.faster.framework.admin.permission.service.PermissionCache;
import cn.org.faster.framework.admin.rolePermission.entity.SysRolePermission;
import cn.org.faster.framework.admin.rolePermission.error.RolePermissionError;
import cn.org.faster.framework.admin.rolePermission.repository.SysRolePermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色-权限关系服务。
 *
 * @author zhangbowen
 */
@Service
@Transactional
public class SysRolePermissionService {

    private final SysRolePermissionRepository rolePermissionRepository;

    private final PermissionCache permissionCache;

    private final AuditSupport auditSupport;

    private final Clock clock;

    public SysRolePermissionService(SysRolePermissionRepository rolePermissionRepository,
                                    PermissionCache permissionCache, AuditSupport auditSupport, Clock clock) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionCache = permissionCache;
        this.auditSupport = auditSupport;
        this.clock = clock;
    }

    /**
     * 全量覆盖角色权限。
     *
     * @param roleId 角色 id
     * @param list   权限关系列表
     */
    public void batchChoose(long roleId, List<SysRolePermission> list) {
        if (roleId == 0L) {
            throw new BusinessException(RolePermissionError.CANNOT_CHOOSE_ADMIN_PERMISSION, HttpStatus.BAD_REQUEST);
        }
        List<Long> permissionIds = list == null ? List.of()
                : list.stream().map(SysRolePermission::getPermissionId).filter(java.util.Objects::nonNull).distinct().toList();
        LocalDateTime now = LocalDateTime.now(clock);
        rolePermissionRepository.replace(roleId, permissionIds, auditSupport.currentUserId(), now);
        permissionCache.clear();
    }

    @Transactional(readOnly = true)
    public List<Long> permissionIds(long roleId) {
        return rolePermissionRepository.findPermissionIds(roleId);
    }

    public void deleteByRoleId(long roleId) {
        rolePermissionRepository.deleteByRoleId(roleId);
    }

    public void deleteByPermissionIds(List<Long> permissionIds) {
        rolePermissionRepository.deleteByPermissionIds(permissionIds);
    }
}
