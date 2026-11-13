package cn.org.faster.framework.admin.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * 可手动推进的时钟，用于验证码/令牌过期等时间相关逻辑的单元测试。
 *
 * @author zhangbowen
 */
public class MutableClock extends Clock {

    private Instant instant;

    private final ZoneId zone;

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public MutableClock(Instant instant) {
        this(instant, ZoneId.of("Asia/Shanghai"));
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public void advance(Duration duration) {
        this.instant = this.instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
