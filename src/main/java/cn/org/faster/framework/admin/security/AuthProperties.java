package cn.org.faster.framework.admin.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证行为配置。
 *
 * @param multipartTerminal 是否允许多端同时登录；{@code false} 表示后登录的令牌会顶掉先前的令牌
 * @param defaultPassword   管理员重置密码后的初始口令
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(Boolean multipartTerminal, String defaultPassword) {

    public AuthProperties {
        if (multipartTerminal == null) {
            multipartTerminal = Boolean.TRUE;
        }
        if (defaultPassword == null || defaultPassword.isBlank()) {
            defaultPassword = "123456";
        }
    }

    /**
     * @return 是否开启单端登录（即 {@code multipart-terminal=false}）
     */
    public boolean singleSession() {
        return !Boolean.TRUE.equals(multipartTerminal);
    }
}
