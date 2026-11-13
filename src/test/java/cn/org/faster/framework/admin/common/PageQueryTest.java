package cn.org.faster.framework.admin.common;

import cn.org.faster.framework.admin.common.model.PageQuery;
import cn.org.faster.framework.admin.common.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 分页参数与结果单元测试。
 *
 * @author zhangbowen
 */
class PageQueryTest {

    @Test
    void appliesDefaults() {
        PageQuery query = new PageQuery();

        assertThat(query.getCurrent()).isEqualTo(1);
        assertThat(query.getSize()).isEqualTo(PageQuery.DEFAULT_SIZE);
        assertThat(query.offset()).isZero();
    }

    @Test
    void clampsPageSizeAndCurrent() {
        PageQuery query = new PageQuery();
        query.setSize(1_000_000);
        query.setCurrent(0);

        assertThat(query.getSize()).isEqualTo(PageQuery.MAX_SIZE);
        assertThat(query.getCurrent()).isEqualTo(1);

        query.setSize(0);
        assertThat(query.getSize()).isEqualTo(1);
    }

    @Test
    void computesOffset() {
        PageQuery query = new PageQuery();
        query.setCurrent(3);
        query.setSize(20);

        assertThat(query.offset()).isEqualTo(40L);
    }

    @Test
    void computesPageResultMetadata() {
        PageQuery query = new PageQuery();
        query.setCurrent(2);
        query.setSize(10);

        PageResult<String> result = PageResult.of(List.of("a"), 25, query);

        assertThat(result.total()).isEqualTo(25);
        assertThat(result.pages()).isEqualTo(3);
        assertThat(result.current()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(10);
    }
}
