package cn.org.faster.framework.admin.permission.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 新增权限请求。
 *
 * @param name     权限名称
 * @param code     权限编码
 * @param parentId 上级权限 id，0 表示根节点
 * @author zhangbowen
 */
public record SysPermissionAddReq(
        @NotBlank(message = "请输入权限名称") String name,
        @NotBlank(message = "请输入权限编码") String code,
        @NotNull(message = "请选择上级id") Long parentId) {
}
