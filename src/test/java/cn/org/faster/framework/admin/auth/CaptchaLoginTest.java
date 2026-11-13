package cn.org.faster.framework.admin.auth;

import cn.org.faster.framework.admin.captcha.service.CaptchaService;
import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证码开启时的登录校验测试。
 *
 * <p>使用独立的内存库与上下文，避免影响其它用例。</p>
 *
 * @author zhangbowen
 */
@org.springframework.boot.test.context.SpringBootTest(properties = {
        "app.captcha.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:faster_admin_captcha;MODE=MySQL;DATABASE_TO_LOWER=TRUE;"
                + "DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class CaptchaLoginTest extends AbstractIntegrationTest {

    @MockitoBean
    private CaptchaService captchaService;

    @Test
    void rejectsLoginWhenCaptchaIsWrong() throws Exception {
        given(captchaService.validate(any(), any())).willReturn(false);

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"admin","password":"admin","captcha":"0000","captchaToken":"t"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1102));
    }

    @Test
    void acceptsLoginWhenCaptchaIsValid() throws Exception {
        given(captchaService.validate(any(), any())).willReturn(true);

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"admin","password":"admin","captcha":"ABCD","captchaToken":"t"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
