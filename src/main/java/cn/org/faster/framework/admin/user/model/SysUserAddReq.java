package cn.org.faster.framework.admin.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增用户请求。
 *
 * @param account  账号
 * @param password 初始密码
 * @param name     姓名
 * @author zhangbowen
 */
public record SysUserAddReq(
        @NotBlank(message = "请输入账号")
        @Size(max = 64, message = "账号不能超过 64 个字符") String account,

        @NotBlank(message = "请输入密码")
        @Size(min = 6, max = 64, message = "密码长度需为 6~64 个字符") String password,

        @NotBlank(message = "请输入姓名")
        @Size(max = 64, message = "姓名不能超过 64 个字符") String name) {
}
