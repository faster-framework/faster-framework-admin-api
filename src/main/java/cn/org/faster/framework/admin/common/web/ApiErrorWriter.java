package cn.org.faster.framework.admin.common.web;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 在 MVC 之外（Spring Security 过滤器链中）输出统一错误结构。
 *
 * <p>认证/授权失败发生在 DispatcherServlet 之前，无法被 {@code @RestControllerAdvice}
 * 捕获，因此需要显式写出响应。</p>
 *
 * @author zhangbowen
 */
@Component
public class ApiErrorWriter {

    private final JsonMapper jsonMapper;

    public ApiErrorWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, ErrorCode errorCode) throws IOException {
        write(request, response, status, errorCode.getCode(), errorCode.getMessage());
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, int code, String message) throws IOException {
        response.resetBuffer();
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(),
                ApiError.of(status.value(), request.getRequestURI(), code, message));
    }
}
