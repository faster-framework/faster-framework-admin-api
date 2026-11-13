package cn.org.faster.framework.admin.captcha.controller;

import cn.org.faster.framework.admin.captcha.service.CaptchaChallenge;
import cn.org.faster.framework.admin.captcha.service.CaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码接口。
 *
 * <p>同时挂在 {@code /admin/captcha} 与 {@code /captcha} 两个前缀下，
 * 兼容旧前端与框架默认路径。</p>
 *
 * @author zhangbowen
 */
@RestController
@RequestMapping({"/admin/captcha", "/captcha"})
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping
    public CaptchaChallenge create() {
        return captchaService.create();
    }

    @GetMapping("/valid")
    public boolean valid(@RequestParam String captcha, @RequestParam String token) {
        return captchaService.validate(captcha, token);
    }
}
