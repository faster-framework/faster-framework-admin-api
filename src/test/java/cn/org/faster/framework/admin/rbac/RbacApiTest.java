package cn.org.faster.framework.admin.rbac;

import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RBAC 集成测试：角色授权、权限即时生效、超管保护。
 *
 * @author zhangbowen
 */
class RbacApiTest extends AbstractIntegrationTest {

    @Test
    void grantedPermissionsTakeEffectImmediatelyAndRevocationIsImmediateToo() throws Exception {
        String adminToken = loginAsAdmin();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String roleName = "角色" + suffix;
        String account = "rbac_" + suffix;
        String userName = "RBAC用户" + suffix;

        createRole(adminToken, roleName);
        long roleId = findRoleId(adminToken, roleName);
        createUser(adminToken, account, userName);
        long userId = findUserId(adminToken, userName);

        mockMvc.perform(withToken(put("/admin/sys/users/" + userId + "/roles/choose"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"list":[{"roleId":%d}]}
                                """.formatted(roleId)))
                .andExpect(status().isNoContent());

        String userToken = login(account, "userpass1");

        // 角色尚未授权：无权限
        mockMvc.perform(withToken(get("/admin/sys/users"), userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1006));

        // 登录即可访问的接口不受影响
        mockMvc.perform(withToken(get("/admin/sys/dict/all"), userToken))
                .andExpect(status().isOk());

        long usersListPermissionId = permissionId(adminToken, "users:list");
        mockMvc.perform(withToken(put("/admin/sys/roles/" + roleId + "/permissions/choose"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"list":[{"permissionId":%d}]}
                                """.formatted(usersListPermissionId)))
                .andExpect(status().isNoContent());

        // 令牌是授权前签发的，但权限即时生效
        mockMvc.perform(withToken(get("/admin/sys/users"), userToken))
                .andExpect(status().isOk());

        // 收回权限后立即失效
        mockMvc.perform(withToken(put("/admin/sys/roles/" + roleId + "/permissions/choose"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"list":[]}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(withToken(get("/admin/sys/users"), userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectsTheBuiltInAdministratorRole() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(withToken(put("/admin/sys/roles/0/permissions/choose"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"list":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1140));

        mockMvc.perform(withToken(delete("/admin/sys/roles/delete"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[0]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1120));
    }

    @Test
    void administratorRoleListsAllPermissionIds() throws Exception {
        String adminToken = loginAsAdmin();

        MvcResult result = mockMvc.perform(withToken(get("/admin/sys/roles/0/permissions"), adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode permissions = readJson(result);
        assertThat(permissions.isArray()).isTrue();
        assertThat(permissions.size()).isGreaterThanOrEqualTo(31);
    }

    private void createRole(String token, String name) throws Exception {
        mockMvc.perform(withToken(post("/admin/sys/roles"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated());
    }

    private void createUser(String token, String account, String name) throws Exception {
        mockMvc.perform(withToken(post("/admin/sys/users"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"%s","password":"userpass1","name":"%s"}
                                """.formatted(account, name)))
                .andExpect(status().isCreated());
    }

    private long findRoleId(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(withToken(get("/admin/sys/roles")
                        .param("name", name), token))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).path("records").get(0).path("id").asLong();
    }

    private long findUserId(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(withToken(get("/admin/sys/users")
                        .param("name", name), token))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).path("records").get(0).path("id").asLong();
    }

    private long permissionId(String token, String code) throws Exception {
        MvcResult result = mockMvc.perform(withToken(get("/admin/sys/permissions"), token))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode node : readJson(result)) {
            if (code.equals(node.path("code").asText())) {
                return node.path("id").asLong();
            }
        }
        throw new IllegalStateException("未找到权限编码: " + code);
    }
}
