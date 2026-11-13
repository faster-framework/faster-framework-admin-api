package cn.org.faster.framework.admin.role.model;

import cn.org.faster.framework.admin.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询条件。
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {

    private String name;
}
