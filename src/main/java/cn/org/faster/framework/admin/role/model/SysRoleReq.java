package cn.org.faster.framework.admin.role.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 角色新增/修改请求。
 *
 * @param name 角色名称
 * @author zhangbowen
 */
public record SysRoleReq(
        @NotBlank(message = "请输入角色名称")
        @Size(max = 64, message = "角色名称不能超过 64 个字符") String name) {
}
