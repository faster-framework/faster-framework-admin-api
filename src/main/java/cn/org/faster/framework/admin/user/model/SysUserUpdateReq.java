package cn.org.faster.framework.admin.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 编辑用户请求（仅允许修改姓名）。
 *
 * @param name 姓名
 * @author zhangbowen
 */
public record SysUserUpdateReq(
        @NotBlank(message = "请输入姓名")
        @Size(max = 64, message = "姓名不能超过 64 个字符") String name) {
}
