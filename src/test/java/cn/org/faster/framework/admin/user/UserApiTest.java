package cn.org.faster.framework.admin.user;

import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理接口集成测试。
 *
 * @author zhangbowen
 */
class UserApiTest extends AbstractIntegrationTest {

    @Test
    void supportsFullUserLifecycle() throws Exception {
        String adminToken = loginAsAdmin();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String account = "user_" + suffix;
        String name = "测试用户" + suffix;

        mockMvc.perform(withToken(post("/admin/sys/users"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"initpass1","name":"%s"}
                                """.formatted(account, name)))
                .andExpect(status().isCreated());

        // 账号唯一
        mockMvc.perform(withToken(post("/admin/sys/users"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"initpass1","name":"%s"}
                                """.formatted(account, name)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1130));

        // 列表：密码字段绝不出现
        MvcResult listResult = mockMvc.perform(withToken(get("/admin/sys/users")
                        .param("name", name)
                        .param("current", "1")
                        .param("size", "10"), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].account").value(account))
                .andReturn();
        assertThat(listResult.getResponse().getContentAsString())
                .doesNotContain("password")
                .doesNotContain("$2a$");

        String userId = readJson(listResult).path("records").get(0).path("id").asText();
        assertThat(userId).isNotBlank();

        // 详情
        mockMvc.perform(withToken(get("/admin/sys/users/" + userId), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").value(account));

        // 修改姓名
        mockMvc.perform(withToken(put("/admin/sys/users/" + userId), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"改名后%s"}
                                """.formatted(suffix)))
                .andExpect(status().isOk());
        mockMvc.perform(withToken(get("/admin/sys/users/" + userId), adminToken))
                .andExpect(jsonPath("$.name").value("改名后" + suffix));

        // 修改密码后旧密码失效
        mockMvc.perform(withToken(put("/admin/sys/users/" + userId + "/password/change"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPwd":"initpass1","password":"newpass123"}
                                """))
                .andExpect(status().isNoContent());
        assertThat(login(account, "newpass123")).isNotBlank();

        // 重置密码为默认口令
        mockMvc.perform(withToken(put("/admin/sys/users/" + userId + "/password/reset"), adminToken))
                .andExpect(status().isNoContent());
        assertThat(login(account, "123456")).isNotBlank();

        // 删除
        mockMvc.perform(withToken(delete("/admin/sys/users/delete"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + userId + "]"))
                .andExpect(status().isNoContent());
        mockMvc.perform(withToken(get("/admin/sys/users/" + userId), adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1131));
    }

    @Test
    void rejectsInvalidPayloadAndMissingUser() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(withToken(post("/admin/sys/users"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));

        mockMvc.perform(withToken(get("/admin/sys/users/999999"), adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1131));
    }

    @Test
    void forbidsDeletingTheBuiltInAdministrator() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(withToken(delete("/admin/sys/users/delete"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[0]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1133));
    }

    @Test
    void clampsOversizedPageRequests() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(withToken(get("/admin/sys/users").param("size", "100000"), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.total").isNumber());
    }
}
