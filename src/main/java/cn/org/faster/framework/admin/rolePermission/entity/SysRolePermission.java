package cn.org.faster.framework.admin.rolePermission.entity;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限关系。
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePermission extends BaseEntity {

    private Long roleId;

    @NotNull(message = "请选择权限")
    private Long permissionId;
}
