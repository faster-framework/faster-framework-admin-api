package cn.org.faster.framework.admin.rolePermission.error;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色权限模块错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum RolePermissionError implements ErrorCode {

    CANNOT_CHOOSE_ADMIN_PERMISSION(1140, "不可为超级管理员选择权限");

    private final int code;

    private final String message;
}
