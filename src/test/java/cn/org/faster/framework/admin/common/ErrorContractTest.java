package cn.org.faster.framework.admin.common;

import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 统一错误响应契约测试。
 *
 * @author zhangbowen
 */
class ErrorContractTest extends AbstractIntegrationTest {

    @Test
    void unknownPathReturnsUnifiedNotFound() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withToken(get("/admin/not-exists"), token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1004))
                .andExpect(jsonPath("$.path").value("/admin/not-exists"));
    }

    @Test
    void unsupportedMethodReturnsUnifiedMethodNotAllowed() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withToken(patch("/admin/sys/users"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(1009));
    }

    @Test
    void malformedJsonReturnsUnifiedParameterError() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(withToken(post("/admin/sys/users"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1001));
    }
}
