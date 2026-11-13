package cn.org.faster.framework.admin.captcha.service;

/**
 * 验证码挑战。
 *
 * @param token 一次性校验 token
 * @param image Base64 编码的 PNG 图片
 * @author zhangbowen
 */
public record CaptchaChallenge(String token, String image) {
}
