package cn.org.faster.framework.admin.user.model;

import cn.org.faster.framework.admin.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询条件。
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    private String name;
}
