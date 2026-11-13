package cn.org.faster.framework.admin.user.controller;

import cn.org.faster.framework.admin.common.model.ListWrapper;
import cn.org.faster.framework.admin.common.model.PageResult;
import cn.org.faster.framework.admin.user.entity.SysUser;
import cn.org.faster.framework.admin.user.model.SysUserAddReq;
import cn.org.faster.framework.admin.user.model.SysUserChangePwdReq;
import cn.org.faster.framework.admin.user.model.SysUserUpdateReq;
import cn.org.faster.framework.admin.user.model.UserQuery;
import cn.org.faster.framework.admin.user.service.SysUserService;
import cn.org.faster.framework.admin.userRole.entity.SysUserRole;
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
 * 用户管理接口。
 *
 * @author zhangbowen
 */
@RestController
@RequestMapping("/admin/sys/users")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users:list')")
    public PageResult<SysUser> list(@ModelAttribute UserQuery query) {
        return userService.list(query);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('users:info')")
    public SysUser info(@PathVariable long userId) {
        return userService.info(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('users:add')")
    public void add(@Valid @RequestBody SysUserAddReq request) {
        userService.add(request);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('users:modify')")
    public void update(@PathVariable long userId, @Valid @RequestBody SysUserUpdateReq request) {
        userService.update(userId, request);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:delete')")
    public void delete(@RequestBody List<Long> userIds) {
        userService.delete(userIds);
    }

    @PutMapping("/{userId}/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:password:change')")
    public void changePassword(@PathVariable long userId, @Valid @RequestBody SysUserChangePwdReq request) {
        userService.changePassword(userId, request);
    }

    @PutMapping("/{userId}/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:password:reset')")
    public void resetPassword(@PathVariable long userId) {
        userService.resetPassword(userId);
    }

    @PutMapping("/{userId}/roles/choose")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:roles:choose')")
    public void chooseRoles(@PathVariable long userId, @Valid @RequestBody ListWrapper<SysUserRole> body) {
        userService.chooseRoles(userId, body.list());
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('users:roles:list')")
    public List<Long> roles(@PathVariable long userId) {
        return userService.roles(userId);
    }
}
