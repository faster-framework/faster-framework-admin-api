package cn.org.faster.framework.admin.upload.service;

/**
 * 上传结果。
 *
 * @param fileName 保存后的文件名
 * @param url      对外访问地址
 * @author zhangbowen
 */
public record UploadResult(String fileName, String url) {
}
