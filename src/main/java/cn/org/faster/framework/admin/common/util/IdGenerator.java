package cn.org.faster.framework.admin.common.util;

import cn.org.faster.framework.admin.common.config.IdProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 雪花算法 id 生成器（41 位时间戳 + 5 位数据中心 + 5 位工作节点 + 12 位序列）。
 *
 * <p>线程安全；时钟回拨时复用上一次时间戳并推进序列，保证 id 单调递增。</p>
 *
 * @author zhangbowen
 */
@Component
public class IdGenerator {

    private static final long EPOCH = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();

    private static final long SEQUENCE_MASK = 0xFFFL;

    private final long workerId;

    private final long dataCenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    public IdGenerator(IdProperties properties) {
        this.workerId = properties.workerId();
        this.dataCenterId = properties.dataCenterId();
    }

    /**
     * @return 全局唯一的正数 id
     */
    public synchronized long next() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            timestamp = lastTimestamp;
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << 22) | (dataCenterId << 17) | (workerId << 12) | sequence;
    }

    private long waitNextMillis(long current) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= current) {
            Thread.onSpinWait();
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
