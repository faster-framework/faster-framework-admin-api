package cn.org.faster.framework.admin.upload;

import cn.org.faster.framework.admin.security.JwtProperties;
import cn.org.faster.framework.admin.support.MutableClock;
import cn.org.faster.framework.admin.upload.service.UploadProperties;
import cn.org.faster.framework.admin.upload.service.UploadToken;
import cn.org.faster.framework.admin.upload.service.UploadTokenService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 预上传凭证单元测试。
 *
 * @author zhangbowen
 */
class UploadTokenServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));

    private final UploadTokenService tokenService = new UploadTokenService(
            new JwtProperties(Base64.getEncoder().encodeToString(
                    "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)),
                    "faster-admin", Duration.ofHours(2), Duration.ofSeconds(30)),
            new UploadProperties("./target/test-upload", "/media", Duration.ofMinutes(30),
                    1024L, Set.of("png")),
            clock);

    @Test
    void issuesVerifiableTokenBoundToUser() {
        UploadToken token = tokenService.issue(7L);

        assertThat(tokenService.verify(token.token(), 7L)).isTrue();
        assertThat(tokenService.verify(token.token(), 8L)).isFalse();
        assertThat(tokenService.verify(token.token() + "tampered", 7L)).isFalse();
        assertThat(tokenService.verify("not-a-token", 7L)).isFalse();
    }

    @Test
    void rejectsExpiredToken() {
        UploadToken token = tokenService.issue(7L);
        clock.advance(Duration.ofMinutes(31));

        assertThat(tokenService.verify(token.token(), 7L)).isFalse();
    }
}
