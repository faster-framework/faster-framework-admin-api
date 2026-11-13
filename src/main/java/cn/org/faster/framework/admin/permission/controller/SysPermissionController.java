package cn.org.faster.framework.admin.permission.controller;

import cn.org.faster.framework.admin.common.entity.TreeNode;
import cn.org.faster.framework.admin.permission.entity.SysPermission;
import cn.org.faster.framework.admin.permission.model.SysPermissionAddReq;
import cn.org.faster.framework.admin.permission.model.SysPermissionUpdateReq;
import cn.org.faster.framework.admin.permission.service.SysPermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理接口。
 *
 * @author zhangbowen
 */
@RestController
@RequestMapping("/admin/sys/permissions")
public class SysPermissionController {

    private final SysPermissionService permissionService;

    public SysPermissionController(SysPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('permissions:tree')")
    public List<TreeNode> tree() {
        return permissionService.tree();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permissions:list')")
    public List<SysPermission> list() {
        return permissionService.list();
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('permissions:info')")
    public SysPermission info(@PathVariable long permissionId) {
        return permissionService.info(permissionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('permissions:add')")
    public void add(@Valid @RequestBody SysPermissionAddReq request) {
        permissionService.add(request);
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('permissions:modify')")
    public void update(@PathVariable long permissionId, @Valid @RequestBody SysPermissionUpdateReq request) {
        permissionService.update(permissionId, request);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('permissions:delete')")
    public void delete(@RequestBody List<Long> permissionIds) {
        permissionService.delete(permissionIds);
    }
}
