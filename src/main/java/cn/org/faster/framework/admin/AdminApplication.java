package cn.org.faster.framework.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Faster-Admin 后端启动类。
 *
 * <p>JDK 17 + Spring Boot 4.1.2 + Spring Framework 7.0.10。</p>
 *
 * @author zhangbowen
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
