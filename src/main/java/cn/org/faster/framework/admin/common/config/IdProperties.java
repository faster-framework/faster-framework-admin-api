package cn.org.faster.framework.admin.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 雪花算法节点配置。
 *
 * <p>多实例部署时必须为每个实例分配不同的 {@code worker-id}，否则同一毫秒内可能产生重复主键。</p>
 *
 * @param workerId     工作节点 id（0~31）
 * @param dataCenterId 数据中心 id（0~31）
 * @author zhangbowen
 */
@ConfigurationProperties(prefix = "app.id")
public record IdProperties(Integer workerId, Integer dataCenterId) {

    public IdProperties {
        if (workerId == null) {
            workerId = 0;
        }
        if (dataCenterId == null) {
            dataCenterId = 0;
        }
        if (workerId < 0 || workerId > 31) {
            throw new IllegalArgumentException("app.id.worker-id 必须在 0~31 之间");
        }
        if (dataCenterId < 0 || dataCenterId > 31) {
            throw new IllegalArgumentException("app.id.data-center-id 必须在 0~31 之间");
        }
    }
}
