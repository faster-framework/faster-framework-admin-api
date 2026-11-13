package cn.org.faster.framework.admin.dict.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 字典新增/修改请求。
 *
 * <p>不再继承 {@code BaseEntity}，杜绝客户端批量赋值
 * {@code id/createBy/deleted} 等字段造成的数据污染。</p>
 *
 * @author zhangbowen
 */
public record SysDictReq(
        @NotBlank(message = "请输入字典名称")
        @Size(max = 128, message = "字典名称不能超过 128 个字符") String name,

        @NotBlank(message = "请输入字典类型")
        @Size(max = 128, message = "字典类型不能超过 128 个字符") String type,

        @NotBlank(message = "请输入字典值") String dictValue,

        @Size(max = 1024, message = "备注不能超过 1024 个字符") String remark,

        @Min(value = 0, message = "展示状态只能为 0 或 1")
        @Max(value = 1, message = "展示状态只能为 0 或 1") Integer showStatus) {
}
