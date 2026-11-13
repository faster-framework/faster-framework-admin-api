package cn.org.faster.framework.admin.permission.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改权限请求（不允许修改上级，避免子树路径重算）。
 *
 * @param name 权限名称
 * @param code 权限编码
 * @author zhangbowen
 */
public record SysPermissionUpdateReq(
        @NotBlank(message = "请输入权限名称") String name,
        @NotBlank(message = "请输入权限编码") String code) {
}
