package cn.org.faster.framework.admin.upload.controller;

import cn.org.faster.framework.admin.upload.service.UploadResult;
import cn.org.faster.framework.admin.upload.service.UploadService;
import cn.org.faster.framework.admin.upload.service.UploadToken;
import cn.org.faster.framework.admin.upload.service.UploadTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传接口。
 *
 * @author zhangbowen
 */
@RestController
@RequestMapping({"/admin/upload", "/upload"})
public class UploadController {

    private final UploadService uploadService;

    private final UploadTokenService uploadTokenService;

    public UploadController(UploadService uploadService, UploadTokenService uploadTokenService) {
        this.uploadService = uploadService;
        this.uploadTokenService = uploadTokenService;
    }

    /**
     * 获取预上传凭证（30 分钟有效）。
     */
    @GetMapping("/preload")
    public UploadToken preload(Authentication authentication) {
        return uploadTokenService.issue(Long.parseLong(authentication.getName()));
    }

    /**
     * 上传文件；{@code token} 可选，携带时会校验预上传凭证。
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResult upload(@RequestPart("file") MultipartFile file,
                               @RequestParam(value = "token", required = false) String token,
                               Authentication authentication) {
        return uploadService.store(file, token, Long.parseLong(authentication.getName()));
    }
}
