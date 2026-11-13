package cn.org.faster.framework.admin.role.service;

import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.common.model.PageResult;
import cn.org.faster.framework.admin.common.util.AuditSupport;
import cn.org.faster.framework.admin.permission.entity.SysPermission;
import cn.org.faster.framework.admin.permission.service.PermissionService;
import cn.org.faster.framework.admin.permission.service.SysPermissionService;
import cn.org.faster.framework.admin.role.entity.SysRole;
import cn.org.faster.framework.admin.role.error.RoleError;
import cn.org.faster.framework.admin.role.model.RoleQuery;
import cn.org.faster.framework.admin.role.model.SysRoleReq;
import cn.org.faster.framework.admin.role.repository.SysRoleRepository;
import cn.org.faster.framework.admin.rolePermission.service.SysRolePermissionService;
import cn.org.faster.framework.admin.userRole.service.SysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色管理服务。
 *
 * @author zhangbowen
 */
@Service
@Transactional
public class SysRoleService {

    private final SysRoleRepository roleRepository;

    private final SysUserRoleService userRoleService;

    private final SysRolePermissionService rolePermissionService;

    private final SysPermissionService permissionService;

    private final PermissionService rbacPermissionService;

    private final AuditSupport auditSupport;

    private final Clock clock;

    public SysRoleService(SysRoleRepository roleRepository, SysUserRoleService userRoleService,
                          SysRolePermissionService rolePermissionService, SysPermissionService permissionService,
                          PermissionService rbacPermissionService, AuditSupport auditSupport, Clock clock) {
        this.roleRepository = roleRepository;
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
        this.rbacPermissionService = rbacPermissionService;
        this.auditSupport = auditSupport;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResult<SysRole> list(RoleQuery query) {
        List<SysRole> records = roleRepository.page(query.getName(), query.getSize(), query.offset());
        long total = roleRepository.count(query.getName());
        return PageResult.of(records, total, query);
    }

    @Transactional(readOnly = true)
    public SysRole info(long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> BusinessException.notFound(RoleError.NOT_EXIST));
    }

    public void add(SysRoleReq request) {
        SysRole role = new SysRole();
        role.setName(request.name());
        auditSupport.markCreated(role);
        roleRepository.insert(role);
    }

    public void update(long roleId, SysRoleReq request) {
        info(roleId);
        SysRole update = new SysRole();
        update.setId(roleId);
        update.setName(request.name());
        auditSupport.markUpdated(update);
        roleRepository.update(update);
        rbacPermissionService.evictAll();
    }

    /**
     * 批量删除角色（超级管理员角色不可删除）。
     *
     * @param roleIds 角色 id 列表
     */
    public void delete(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Long> deletable = new ArrayList<>();
        for (Long roleId : roleIds) {
            if (roleId == null) {
                continue;
            }
            if (roleId == PermissionService.ADMIN_ROLE_ID) {
                throw BusinessException.badRequest(RoleError.ADMIN_CANNOT_DELETE);
            }
            deletable.add(roleId);
        }
        roleRepository.softDelete(deletable, auditSupport.currentUserId(), LocalDateTime.now(clock));
        for (Long roleId : deletable) {
            userRoleService.deleteByRoleId(roleId);
            rolePermissionService.deleteByRoleId(roleId);
        }
        rbacPermissionService.evictAll();
    }

    /**
     * 角色已选择的权限 id；超级管理员返回全部权限 id。
     *
     * @param roleId 角色 id
     * @return 权限 id 列表
     */
    @Transactional(readOnly = true)
    public List<Long> permissions(long roleId) {
        if (roleId == PermissionService.ADMIN_ROLE_ID) {
            return permissionService.list().stream().map(SysPermission::getId).toList();
        }
        return rolePermissionService.permissionIds(roleId);
    }
}
