package cn.org.faster.framework.admin.upload.error;

import cn.org.faster.framework.admin.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 上传模块错误码。
 *
 * @author zhangbowen
 */
@Getter
@AllArgsConstructor
public enum UploadError implements ErrorCode {

    INVALID_FILE(1150, "文件不合法"),

    FILE_TOO_LARGE(1151, "文件超出大小限制"),

    INVALID_TOKEN(1152, "上传凭证无效或已过期"),

    IO_ERROR(1153, "文件保存失败");

    private final int code;

    private final String message;
}
