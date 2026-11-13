package cn.org.faster.framework.admin.captcha;

import cn.org.faster.framework.admin.captcha.service.CaptchaChallenge;
import cn.org.faster.framework.admin.captcha.service.CaptchaProperties;
import cn.org.faster.framework.admin.captcha.service.CaptchaService;
import cn.org.faster.framework.admin.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证码单元测试。
 *
 * @author zhangbowen
 */
class CaptchaServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));

    private final CaptchaService captchaService = new CaptchaService(
            new CaptchaProperties(true, Duration.ofSeconds(60), 4, 120, 40), clock);

    @Test
    void generatesBase64PngChallenge() {
        CaptchaChallenge challenge = captchaService.create();

        assertThat(challenge.token()).isNotBlank();
        byte[] png = Base64.getDecoder().decode(challenge.image());
        assertThat(png).isNotEmpty();
        // PNG 魔数
        assertThat(png[0] & 0xFF).isEqualTo(0x89);
        assertThat(new String(png, 1, 3)).isEqualTo("PNG");
    }

    @Test
    void answerCanOnlyBeUsedOnce() {
        CaptchaChallenge challenge = captchaService.create();
        String answer = answerOf(challenge.token());

        assertThat(captchaService.validate(answer, challenge.token())).isTrue();
        assertThat(captchaService.validate(answer, challenge.token())).isFalse();
    }

    @Test
    void rejectsWrongOrExpiredAnswers() {
        CaptchaChallenge challenge = captchaService.create();
        assertThat(captchaService.validate("0000", challenge.token())).isFalse();

        CaptchaChallenge expired = captchaService.create();
        clock.advance(Duration.ofSeconds(61));
        assertThat(captchaService.validate(answerOf(expired.token()), expired.token())).isFalse();

        assertThat(captchaService.validate(null, null)).isFalse();
    }

    private String answerOf(String token) {
        // 通过反射读取内部答案，仅用于测试
        try {
            var field = CaptchaService.class.getDeclaredField("challenges");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var map = (java.util.Map<String, Object>) field.get(captchaService);
            Object challenge = map.get(token);
            var answer = challenge.getClass().getDeclaredMethod("answer");
            answer.setAccessible(true);
            return (String) answer.invoke(challenge);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
