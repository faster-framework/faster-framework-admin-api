package cn.org.faster.framework.admin.common.web;

import cn.org.faster.framework.admin.common.error.ErrorCode;

/**
 * 统一错误响应体。
 *
 * <p>字段与旧版框架 {@code ResponseErrorEntity} 完全一致，保证前端拦截器无需改动。</p>
 *
 * @param timestamp 服务端时间戳（毫秒）
 * @param status    HTTP 状态码
 * @param path      请求路径
 * @param code      业务错误码
 * @param message   错误信息
 * @author zhangbowen
 */
public record ApiError(long timestamp, int status, String path, int code, String message) {

    public static ApiError of(int status, String path, ErrorCode errorCode) {
        return new ApiError(System.currentTimeMillis(), status, path, errorCode.getCode(), errorCode.getMessage());
    }

    public static ApiError of(int status, String path, int code, String message) {
        return new ApiError(System.currentTimeMillis(), status, path, code, message);
    }
}
