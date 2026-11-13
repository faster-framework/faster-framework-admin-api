package cn.org.faster.framework.admin.common.error;

/**
 * 业务错误码契约。
 *
 * <p>错误码与 HTTP 状态码解耦：错误码用于前端提示与国际化，HTTP 状态码表达协议语义。</p>
 *
 * @author zhangbowen
 */
public interface ErrorCode {

    /**
     * @return 业务错误码
     */
    int getCode();

    /**
     * @return 默认提示信息
     */
    String getMessage();
}
