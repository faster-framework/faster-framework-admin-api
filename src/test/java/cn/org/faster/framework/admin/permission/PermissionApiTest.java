package cn.org.faster.framework.admin.permission;

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
 * 权限管理接口集成测试。
 *
 * @author zhangbowen
 */
class PermissionApiTest extends AbstractIntegrationTest {

    @Test
    void buildsTreeAndManagesSubtree() throws Exception {
        String token = loginAsAdmin();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        MvcResult tree = mockMvc.perform(withToken(get("/admin/sys/permissions/tree"), token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode roots = readJson(tree);
        assertThat(roots.isArray()).isTrue();
        assertThat(roots.size()).isGreaterThanOrEqualTo(4);
        assertThat(roots.get(0).path("children").isArray()).isTrue();

        // 新增父权限
        mockMvc.perform(withToken(post("/admin/sys/permissions"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"测试父权限","code":"test:parent:%s","parentId":0}
                                """.formatted(suffix)))
                .andExpect(status().isCreated());
        long parentId = permissionId(token, "test:parent:" + suffix);

        // 新增子权限
        mockMvc.perform(withToken(post("/admin/sys/permissions"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"测试子权限","code":"test:child:%s","parentId":%d}
                                """.formatted(suffix, parentId)))
                .andExpect(status().isCreated());

        // 树中包含父子结构
        MvcResult updatedTree = mockMvc.perform(withToken(get("/admin/sys/permissions/tree"), token))
                .andReturn();
        assertThat(readJson(updatedTree).toString()).contains("test:child:" + suffix);

        // 删除父权限会级联删除子权限
        mockMvc.perform(withToken(delete("/admin/sys/permissions/delete"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + parentId + "]"))
                .andExpect(status().isNoContent());
        MvcResult flat = mockMvc.perform(withToken(get("/admin/sys/permissions"), token)).andReturn();
        String body = flat.getResponse().getContentAsString();
        assertThat(body).doesNotContain("test:parent:" + suffix);
        assertThat(body).doesNotContain("test:child:" + suffix);
    }

    @Test
    void validatesCodeUniquenessAndParentExistence() throws Exception {
        String token = loginAsAdmin();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(withToken(post("/admin/sys/permissions"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"重复编码","code":"test:dup:%s","parentId":0}
                                """.formatted(suffix)))
                .andExpect(status().isCreated());

        mockMvc.perform(withToken(post("/admin/sys/permissions"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"重复编码","code":"test:dup:%s","parentId":0}
                                """.formatted(suffix)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1111));

        mockMvc.perform(withToken(post("/admin/sys/permissions"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"父级不存在","code":"test:orphan:%s","parentId":999999}
                                """.formatted(suffix)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1110));
    }

    @Test
    void permissionInfoReturnsNotFoundForUnknownId() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withToken(get("/admin/sys/permissions/999999"), token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1112));
    }

    private long permissionId(String token, String code) throws Exception {
        MvcResult result = mockMvc.perform(withToken(get("/admin/sys/permissions"), token)).andReturn();
        for (JsonNode node : readJson(result)) {
            if (code.equals(node.path("code").asText())) {
                return node.path("id").asLong();
            }
        }
        throw new IllegalStateException("未找到权限: " + code);
    }
}
