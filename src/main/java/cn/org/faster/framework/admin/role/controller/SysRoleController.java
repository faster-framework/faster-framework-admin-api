package cn.org.faster.framework.admin.role.controller;

import cn.org.faster.framework.admin.common.model.ListWrapper;
import cn.org.faster.framework.admin.common.model.PageResult;
import cn.org.faster.framework.admin.role.entity.SysRole;
import cn.org.faster.framework.admin.role.model.RoleQuery;
import cn.org.faster.framework.admin.role.model.SysRoleReq;
import cn.org.faster.framework.admin.role.service.SysRoleService;
import cn.org.faster.framework.admin.rolePermission.entity.SysRolePermission;
import cn.org.faster.framework.admin.rolePermission.service.SysRolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口。
 *
 * @author zhangbowen
 */
@RestController
@RequestMapping("/admin/sys/roles")
public class SysRoleController {

    private final SysRoleService roleService;

    private final SysRolePermissionService rolePermissionService;

    public SysRoleController(SysRoleService roleService, SysRolePermissionService rolePermissionService) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('roles:list')")
    public PageResult<SysRole> list(@ModelAttribute RoleQuery query) {
        return roleService.list(query);
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('roles:info')")
    public SysRole info(@PathVariable long roleId) {
        return roleService.info(roleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('roles:add')")
    public void add(@Valid @RequestBody SysRoleReq request) {
        roleService.add(request);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('roles:modify')")
    public void update(@PathVariable long roleId, @Valid @RequestBody SysRoleReq request) {
        roleService.update(roleId, request);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('roles:delete')")
    public void delete(@RequestBody List<Long> roleIds) {
        roleService.delete(roleIds);
    }

    @PutMapping("/{roleId}/permissions/choose")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('roles:permissions:choose')")
    public void choosePermissions(@PathVariable long roleId,
                                  @Valid @RequestBody ListWrapper<SysRolePermission> body) {
        rolePermissionService.batchChoose(roleId, body.list());
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('roles:permissions:list')")
    public List<Long> permissions(@PathVariable long roleId) {
        return roleService.permissions(roleId);
    }
}
