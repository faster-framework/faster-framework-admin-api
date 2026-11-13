package cn.org.faster.framework.admin.role.entity;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体。
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    private String name;
}
