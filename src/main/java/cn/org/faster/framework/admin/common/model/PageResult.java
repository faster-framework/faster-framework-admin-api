package cn.org.faster.framework.admin.common.model;

import java.util.List;

/**
 * 分页结果。
 *
 * <p>字段名与旧版 MyBatis-Plus {@code IPage} 的 JSON 输出保持一致
 * （records / total / size / current / pages），前端无需改动。</p>
 *
 * @param records 当前页数据
 * @param total   总记录数
 * @param size    每页条数
 * @param current 当前页码（从 1 开始）
 * @param pages   总页数
 * @param <T>     记录类型
 * @author zhangbowen
 */
public record PageResult<T>(List<T> records, long total, long size, long current, long pages) {

    public static <T> PageResult<T> of(List<T> records, long total, PageQuery query) {
        int size = query.getSize();
        long pages = total == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(records, total, size, query.getCurrent(), pages);
    }
}
