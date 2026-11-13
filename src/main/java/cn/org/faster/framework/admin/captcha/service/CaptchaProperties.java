package cn.org.faster.framework.admin.captcha.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 图形验证码配置。
 *
 * @param enabled 是否启用验证码校验（仅供本地联调/自动化测试关闭，生产环境必须开启）
 * @param ttl     验证码有效期
 * @param length  字符个数
 * @param width   图片宽度
 * @param height  图片高度
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.captcha")
public record CaptchaProperties(Boolean enabled, Duration ttl, Integer length, Integer width, Integer height) {

    public CaptchaProperties {
        if (enabled == null) {
            enabled = Boolean.TRUE;
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofSeconds(60);
        }
        if (length == null || length < 4 || length > 8) {
            length = 4;
        }
        if (width == null || width < 60) {
            width = 120;
        }
        if (height == null || height < 30) {
            height = 40;
        }
    }
}
