package cn.org.faster.framework.admin.media.controller;

import cn.org.faster.framework.admin.upload.service.UploadService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 媒体文件下载。
 *
 * @author zhangbowen
 */
@RestController
public class MediaController {

    private final UploadService uploadService;

    public MediaController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping({"/admin/media/{fileName}/_download", "/media/{fileName}/_download"})
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        Path path = uploadService.resolve(fileName);
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        String disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(new FileSystemResource(path));
    }
}
