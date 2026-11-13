package cn.org.faster.framework.admin.upload.service;

/**
 * 预上传凭证。
 *
 * @param token     凭证
 * @param expiresAt 过期时间（毫秒时间戳）
 * @author zhangbowen
 */
public record UploadToken(String token, long expiresAt) {
}
