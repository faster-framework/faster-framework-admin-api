package cn.org.faster.framework.admin.upload;

import cn.org.faster.framework.admin.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 上传接口集成测试。
 *
 * @author zhangbowen
 */
class UploadApiTest extends AbstractIntegrationTest {

    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    private static final Path UPLOAD_DIR = Path.of("./target/test-upload").toAbsolutePath().normalize();

    @AfterEach
    void cleanUploadedFiles() throws IOException {
        if (!Files.isDirectory(UPLOAD_DIR)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(UPLOAD_DIR)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(UPLOAD_DIR))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // 清理失败不影响用例结论
                        }
                    });
        }
    }

    @Test
    void uploadsAndDownloadsFileWithPreloadToken() throws Exception {
        String adminToken = loginAsAdmin();

        MvcResult preload = mockMvc.perform(withToken(get("/admin/upload/preload"), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String uploadToken = readJson(preload).path("token").asText();

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", PNG);
        MvcResult upload = mockMvc.perform(multipart("/admin/upload").file(file)
                        .param("token", uploadToken).header("Auth-Token", adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").isNotEmpty())
                .andExpect(jsonPath("$.url").isNotEmpty())
                .andReturn();
        JsonNode body = readJson(upload);
        String fileName = body.path("fileName").asText();
        String url = body.path("url").asText();

        assertThat(url).isEqualTo("/media/" + fileName);
        assertThat(Files.exists(UPLOAD_DIR.resolve(fileName))).isTrue();

        mockMvc.perform(withToken(get("/admin/media/" + fileName + "/_download"), adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(PNG));
    }

    @Test
    void rejectsDisallowedExtensionAndForgedToken() throws Exception {
        String adminToken = loginAsAdmin();

        MockMultipartFile executable = new MockMultipartFile("file", "evil.exe", "application/octet-stream",
                new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/admin/upload").file(executable).header("Auth-Token", adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1150));

        MockMultipartFile png = new MockMultipartFile("file", "avatar.png", "image/png", PNG);
        mockMvc.perform(multipart("/admin/upload").file(png)
                        .param("token", "forged-token").header("Auth-Token", adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1152));
    }

    @Test
    void downloadRejectsPathTraversal() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(withToken(get("/admin/media/..%2Fpom.xml/_download"), adminToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void uploadRequiresAuthentication() throws Exception {
        MockMultipartFile png = new MockMultipartFile("file", "avatar.png", "image/png", PNG);
        mockMvc.perform(multipart("/admin/upload").file(png).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }
}
