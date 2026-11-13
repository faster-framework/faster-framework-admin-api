package cn.org.faster.framework.admin.auth;

import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证接口集成测试。
 *
 * @author zhangbowen
 */
class AuthApiTest extends AbstractIntegrationTest {

    @Test
    void logoutRevokesTheToken() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withToken(delete("/admin/logout"), token))
                .andExpect(status().isNoContent());

        mockMvc.perform(withToken(get("/admin/userInfo"), token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1002));
    }

    @Test
    void rejectsForgedAndMalformedTokens() throws Exception {
        mockMvc.perform(withToken(get("/admin/userInfo"), "not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1002));

        mockMvc.perform(withToken(get("/admin/userInfo"), "a.b.c"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void supportAuthorizationBearerHeaderAsWell() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/admin/userInfo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").value("admin"));
    }

    @Test
    void captchaEndpointIsPublicAndReturnsPng() throws Exception {
        var result = mockMvc.perform(get("/admin/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String image = readJson(result).path("image").asText();
        assertThat(image).isNotBlank();
        assertThat(java.util.Base64.getDecoder().decode(image)).isNotEmpty();
    }

    @Test
    void loginRequiresValidBody() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("请填写用户名")));
    }
}
