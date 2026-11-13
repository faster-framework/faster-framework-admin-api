package cn.org.faster.framework.admin.dict;

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
 * 字典接口集成测试。
 *
 * @author zhangbowen
 */
class DictApiTest extends AbstractIntegrationTest {

    @Test
    void supportsCrudAndConditionalQuery() throws Exception {
        String token = loginAsAdmin();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "dict_" + suffix;

        mockMvc.perform(withToken(post("/admin/sys/dict"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","type":"gender","dictValue":"[{\\"label\\":\\"男\\",\\"value\\":1}]","remark":"备注"}
                                """.formatted(name)))
                .andExpect(status().isCreated());

        // 分页列表
        MvcResult listResult = mockMvc.perform(withToken(get("/admin/sys/dict")
                        .param("name", name)
                        .param("type", "gender"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andReturn();
        long id = readJson(listResult).path("records").get(0).path("id").asLong();

        // 条件查询
        mockMvc.perform(withToken(get("/admin/sys/dict/query")
                        .param("name", name)
                        .param("type", "gender"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictValue").value("[{\"label\":\"男\",\"value\":1}]"));

        // 更新
        mockMvc.perform(withToken(put("/admin/sys/dict/" + id), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","type":"gender","dictValue":"[]","remark":"已更新","showStatus":0}
                                """.formatted(name)))
                .andExpect(status().isOk());
        mockMvc.perform(withToken(get("/admin/sys/dict/" + id), token))
                .andExpect(jsonPath("$.remark").value("已更新"))
                .andExpect(jsonPath("$.showStatus").value(0));

        // 删除
        mockMvc.perform(withToken(delete("/admin/sys/dict/delete"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + id + "]"))
                .andExpect(status().isNoContent());
        mockMvc.perform(withToken(get("/admin/sys/dict/" + id), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void ignoresMassAssignmentFieldsInRequest() throws Exception {
        String token = loginAsAdmin();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "mass_" + suffix;

        mockMvc.perform(withToken(post("/admin/sys/dict"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":123,"deleted":1,"createBy":999,"name":"%s","type":"t","dictValue":"v"}
                                """.formatted(name)))
                .andExpect(status().isCreated());

        MvcResult all = mockMvc.perform(withToken(get("/admin/sys/dict/all"), token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode records = readJson(all);
        for (JsonNode node : records) {
            assertThat(node.path("id").asLong()).isNotEqualTo(123L);
            assertThat(node.path("deleted").asInt()).isZero();
        }
    }

    @Test
    void validatesRequiredFields() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withToken(post("/admin/sys/dict"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"t","dictValue":"v"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("请输入字典名称")));
    }
}
