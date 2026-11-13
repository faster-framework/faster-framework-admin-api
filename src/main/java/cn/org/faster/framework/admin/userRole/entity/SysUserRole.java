package cn.org.faster.framework.admin.userRole.entity;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-角色关系。
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserRole extends BaseEntity {

    private Long userId;

    @NotNull(message = "请选择角色")
    private Long roleId;
}
