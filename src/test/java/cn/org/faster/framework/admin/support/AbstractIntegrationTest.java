package cn.org.faster.framework.admin.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 集成测试基类：H2 内存库 + 完整 Spring 上下文 + MockMvc。
 *
 * <p>测试方法运行在事务中并自动回滚，避免用例之间互相污染。</p>
 *
 * @author zhangbowen
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    protected String loginAsAdmin() throws Exception {
        return login("admin", "admin");
    }

    protected String login(String account, String password) throws Exception {
        String body = """
                {"account":"%s","password":"%s","captcha":"AAAA","captchaToken":"token"}
                """.formatted(account, password);
        MvcResult result = mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        JsonNode node = jsonMapper.readTree(result.getResponse().getContentAsString());
        return node.path("token").asText();
    }

    protected MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Auth-Token", token);
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return jsonMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String toJson(Object value) {
        return jsonMapper.writeValueAsString(value);
    }
}
