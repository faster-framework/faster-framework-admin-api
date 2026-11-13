package cn.org.faster.framework.admin.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求。
 *
 * @param account      账号
 * @param password     密码
 * @param captcha      图形验证码
 * @param captchaToken 图形验证码 token
 * @author zhangbowen
 */
public record LoginReq(
        @NotBlank(message = "请填写用户名")
        @Size(max = 64, message = "账号不能超过 64 个字符") String account,

        @NotBlank(message = "请填写密码")
        @Size(max = 64, message = "密码不能超过 64 个字符") String password,

        @NotBlank(message = "请填写验证码") String captcha,

        @NotBlank(message = "图形验证码token错误") String captchaToken) {
}
