package cn.org.faster.framework.admin.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 配置。
 *
 * @param allowedOrigins 允许的来源，支持通配符（如 {@code https://*.example.com}）；为空时放行全部
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of();
        }
    }
}
