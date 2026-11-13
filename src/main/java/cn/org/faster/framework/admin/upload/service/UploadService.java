package cn.org.faster.framework.admin.upload.service;

import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.upload.error.UploadError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 本地文件存储服务。
 *
 * <p>安全约束：扩展名白名单、大小上限、文件名随机生成、路径穿越校验。</p>
 *
 * @author zhangbowen
 */
@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private static final Pattern SAFE_FILE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,127}");

    private final UploadProperties properties;

    private final UploadTokenService uploadTokenService;

    public UploadService(UploadProperties properties, UploadTokenService uploadTokenService) {
        this.properties = properties;
        this.uploadTokenService = uploadTokenService;
    }

    /**
     * 保存上传文件。
     *
     * @param file   上传文件
     * @param token  预上传凭证（可为空表示未走预上传流程）
     * @param userId 当前用户
     * @return 文件名与访问地址
     */
    public UploadResult store(MultipartFile file, String token, long userId) {
        if (StringUtils.hasText(token) && !uploadTokenService.verify(token, userId)) {
            throw BusinessException.badRequest(UploadError.INVALID_TOKEN);
        }
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest(UploadError.INVALID_FILE);
        }
        if (file.getSize() > properties.maxSize()) {
            throw new BusinessException(UploadError.FILE_TOO_LARGE, HttpStatus.PAYLOAD_TOO_LARGE);
        }
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        if (!StringUtils.hasText(extension)
                || !properties.allowedExtensions().contains(extension.toLowerCase(Locale.ROOT))) {
            throw BusinessException.badRequest(UploadError.INVALID_FILE);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "")
                + "." + extension.toLowerCase(Locale.ROOT);
        Path root = properties.root();
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw BusinessException.badRequest(UploadError.INVALID_FILE);
        }
        try {
            Files.createDirectories(root);
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("保存上传文件失败: {}", target, ex);
            throw new BusinessException(UploadError.IO_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new UploadResult(fileName, properties.urlPrefix() + "/" + fileName);
    }

    /**
     * 解析已保存文件；文件名只允许单段安全字符。
     *
     * @param fileName 文件名
     * @return 文件路径
     */
    public Path resolve(String fileName) {
        if (!StringUtils.hasText(fileName) || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw BusinessException.badRequest(UploadError.INVALID_FILE);
        }
        Path root = properties.root();
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) {
            throw BusinessException.notFound(UploadError.INVALID_FILE);
        }
        return path;
    }
}
