package cn.org.faster.framework.admin.auth.error;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum AuthError implements ErrorCode {

    /**
     * 保留给旧前端映射；出于防止用户名枚举的考虑，登录接口不再返回该错误码。
     */
    @Deprecated
    USER_NOT_EXIST(1100, "用户未注册"),

    PASSWORD_ERROR(1101, "账号或密码错误"),

    CAPTCHA_ERROR(1102, "验证码错误或已过期");

    private final int code;

    private final String message;
}
