package cn.org.faster.framework.admin.role.error;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色模块错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum RoleError implements ErrorCode {

    ADMIN_CANNOT_DELETE(1120, "超级管理员角色不可删除"),

    NOT_EXIST(1121, "角色不存在");

    private final int code;

    private final String message;
}
