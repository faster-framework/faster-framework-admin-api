package cn.org.faster.framework.admin.common.error;

import org.springframework.http.HttpStatus;

/**
 * 业务异常：携带业务错误码与期望的 HTTP 状态码。
 *
 * <p>业务代码只抛该异常，由 {@code GlobalExceptionHandler} 统一转换为
 * {@code {timestamp,status,path,code,message}} 结构。</p>
 *
 * @author zhangbowen
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    private final HttpStatus status;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException badRequest(ErrorCode errorCode) {
        return new BusinessException(errorCode, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException unauthorized(ErrorCode errorCode) {
        return new BusinessException(errorCode, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException forbidden(ErrorCode errorCode) {
        return new BusinessException(errorCode, HttpStatus.FORBIDDEN);
    }

    public static BusinessException notFound(ErrorCode errorCode) {
        return new BusinessException(errorCode, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(ErrorCode errorCode) {
        return new BusinessException(errorCode, HttpStatus.CONFLICT);
    }
}
