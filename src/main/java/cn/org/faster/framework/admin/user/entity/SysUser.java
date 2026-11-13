package cn.org.faster.framework.admin.user.entity;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体。
 *
 * <p>{@code password} 永不参与 JSON 序列化，避免哈希泄露到前端、日志与浏览器缓存。</p>
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    private String account;

    @JsonIgnore
    private String password;

    private String name;
}
