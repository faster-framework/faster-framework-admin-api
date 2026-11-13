package cn.org.faster.framework.admin.common.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 批量选择请求包装，请求体形如 {@code {"list":[...]}}。
 *
 * @param list 元素列表
 * @param <T>  元素类型
 * @author zhangbowen
 */
public record ListWrapper<T>(@NotNull(message = "请选择数据") @Valid List<T> list) {
}
