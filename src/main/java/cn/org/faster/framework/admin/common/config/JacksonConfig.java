package cn.org.faster.framework.admin.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Jackson 3 序列化配置。
 *
 * <p>雪花 id 是 19 位长整型，超出 JavaScript {@code Number.MAX_SAFE_INTEGER}，
 * 因此所有 {@code Long} 统一序列化为字符串，避免前端精度丢失。</p>
 *
 * @author zhangbowen
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    @Bean
    public JacksonModule longToStringModule() {
        SimpleModule module = new SimpleModule("faster-admin-long-to-string");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return module;
    }
}
