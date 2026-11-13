package cn.org.faster.framework.admin.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求。
 *
 * @param oldPwd   旧密码
 * @param password 新密码
 * @author zhangbowen
 */
public record SysUserChangePwdReq(
        @NotBlank(message = "请输入旧密码") String oldPwd,

        @NotBlank(message = "请输入新密码")
        @Size(min = 6, max = 64, message = "密码长度需为 6~64 个字符") String password) {
}
