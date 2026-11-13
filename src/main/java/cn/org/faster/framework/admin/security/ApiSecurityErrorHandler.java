package cn.org.faster.framework.admin.security;

import cn.org.faster.framework.admin.common.error.BasicErrorCode;
import cn.org.faster.framework.admin.common.web.ApiErrorWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 过滤器链中的认证/授权失败处理。
 *
 * <p>输出与 MVC 层一致的结构：401 对应 {@code 1002 登录状态过期}，
 * 403 对应 {@code 1006 权限不足}。</p>
 *
 * @author zhangbowen
 */
@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiErrorWriter errorWriter;

    public ApiSecurityErrorHandler(ApiErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, BasicErrorCode.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        errorWriter.write(request, response, HttpStatus.FORBIDDEN, BasicErrorCode.FORBIDDEN);
    }
}
