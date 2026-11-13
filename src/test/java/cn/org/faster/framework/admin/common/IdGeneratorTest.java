package cn.org.faster.framework.admin.common;

import cn.org.faster.framework.admin.common.config.IdProperties;
import cn.org.faster.framework.admin.common.util.IdGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 雪花 id 生成器单元测试。
 *
 * @author zhangbowen
 */
class IdGeneratorTest {

    @Test
    void generatesUniquePositiveIncreasingIds() {
        IdGenerator generator = new IdGenerator(new IdProperties(1, 2));
        Set<Long> ids = new HashSet<>();
        long previous = 0;

        for (int i = 0; i < 5_000; i++) {
            long id = generator.next();
            assertThat(id).isPositive().isGreaterThan(previous);
            assertThat(ids.add(id)).isTrue();
            previous = id;
        }
    }

    @Test
    void differentWorkersProduceDifferentIds() {
        IdGenerator first = new IdGenerator(new IdProperties(1, 1));
        IdGenerator second = new IdGenerator(new IdProperties(2, 1));

        assertThat(first.next()).isNotEqualTo(second.next());
    }
}
