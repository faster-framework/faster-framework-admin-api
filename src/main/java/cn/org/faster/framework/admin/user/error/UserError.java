package cn.org.faster.framework.admin.user.error;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum UserError implements ErrorCode {

    USER_EXIST(1130, "用户已存在"),

    USER_NOT_EXIST(1131, "用户不存在"),

    OLD_PASSWORD_ERROR(1132, "旧密码输入错误"),

    ADMIN_CANNOT_DELETE(1133, "超级管理员用户不可删除"),

    PASSWORD_NOT_CHANGED(1134, "新密码不能与旧密码相同");

    private final int code;

    private final String message;
}
