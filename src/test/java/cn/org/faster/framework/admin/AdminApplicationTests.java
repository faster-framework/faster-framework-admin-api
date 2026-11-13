package cn.org.faster.framework.admin;

import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 应用启动与认证冒烟测试。
 *
 * @author zhangbowen
 */
class AdminApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoadsAndAdminCanLogin() throws Exception {
        String token = loginAsAdmin();
        assertThat(token).isNotBlank();

        mockMvc.perform(withToken(get("/admin/userInfo"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").value("admin"))
                .andExpect(jsonPath("$.name").value("超级管理员"))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions[?(@ == 'users:list')]").exists());
    }

    @Test
    void unauthenticatedRequestIsRejectedWithUnifiedError() throws Exception {
        mockMvc.perform(get("/admin/sys/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/admin/sys/users"))
                .andExpect(jsonPath("$.message").value("登录状态过期"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"admin","password":"wrong-password","captcha":"AAAA","captchaToken":"token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1101));
    }

    @Test
    void longIdsAreSerializedAsStrings() throws Exception {
        String token = loginAsAdmin();
        mockMvc.perform(withToken(get("/admin/sys/roles/0"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id").value("0"));
    }
}
