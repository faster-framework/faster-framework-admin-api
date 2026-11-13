package cn.org.faster.framework.admin.upload.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 本地上传配置。
 *
 * @param dir               文件保存目录
 * @param urlPrefix         对外访问前缀，与静态资源映射一致
 * @param tokenTtl          预上传凭证有效期
 * @param maxSize           单文件最大字节数
 * @param allowedExtensions 允许的扩展名（小写，不带点）
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(String dir, String urlPrefix, Duration tokenTtl, Long maxSize,
                               Set<String> allowedExtensions) {

    public UploadProperties {
        if (dir == null || dir.isBlank()) {
            dir = "./data/upload";
        }
        if (urlPrefix == null || urlPrefix.isBlank()) {
            urlPrefix = "/media";
        }
        if (tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative()) {
            tokenTtl = Duration.ofMinutes(30);
        }
        if (maxSize == null || maxSize <= 0) {
            maxSize = 10L * 1024 * 1024;
        }
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            allowedExtensions = new LinkedHashSet<>(Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf",
                    "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "txt"));
        } else {
            allowedExtensions = allowedExtensions.stream().map(String::toLowerCase).collect(
                    java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /**
     * @return 规范化后的绝对目录
     */
    public Path root() {
        return Path.of(dir).toAbsolutePath().normalize();
    }
}
