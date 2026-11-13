package cn.org.faster.framework.admin.permission.error;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限模块错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum SysPermissionError implements ErrorCode {

    PARENT_NOT_EXIST(1110, "权限父级不存在"),

    CODE_EXIST(1111, "权限编码已经存在"),

    NOT_EXIST(1112, "权限不存在");

    private final int code;

    private final String message;
}
