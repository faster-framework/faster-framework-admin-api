package cn.org.faster.framework.admin.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum BasicErrorCode implements ErrorCode {

    /**
     * 未捕获异常兜底。
     */
    INTERNAL_ERROR(1000, "处理失败"),

    /**
     * 参数校验失败。
     */
    PARAM_ERROR(1001, "参数异常"),

    /**
     * 未认证或令牌失效。
     */
    UNAUTHORIZED(1002, "登录状态过期"),

    /**
     * 资源不存在。
     */
    RESOURCE_NOT_FOUND(1004, "资源不存在"),

    /**
     * API 版本过时。
     */
    API_VERSION_OUTDATED(1005, "版本过时"),

    /**
     * 已认证但权限不足。
     */
    FORBIDDEN(1006, "权限不足"),

    /**
     * 维护中。
     */
    MAINTENANCE(1007, "服务器正在维护"),

    /**
     * 唯一索引冲突。
     */
    DATA_CONFLICT(1008, "数据冲突"),

    /**
     * 请求方法不支持。
     */
    METHOD_NOT_ALLOWED(1009, "请求方法不支持");

    private final int code;

    private final String message;
}
