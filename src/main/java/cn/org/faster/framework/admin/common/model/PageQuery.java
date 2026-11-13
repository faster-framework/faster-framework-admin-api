package cn.org.faster.framework.admin.common.model;

import lombok.Data;

/**
 * 分页查询参数。
 *
 * <p>沿用前端既有的 {@code current} / {@code size} 参数名；{@code size} 被钳制在
 * 1~100，避免单次请求加载整表。</p>
 *
 * @author zhangbowen
 */
@Data
public class PageQuery {

    public static final int DEFAULT_SIZE = 10;

    public static final int MAX_SIZE = 100;

    private int current = 1;

    private int size = DEFAULT_SIZE;

    public int getCurrent() {
        return Math.max(current, 1);
    }

    public int getSize() {
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }

    /**
     * @return 从 0 开始的行偏移量
     */
    public long offset() {
        return (long) (getCurrent() - 1) * getSize();
    }
}
