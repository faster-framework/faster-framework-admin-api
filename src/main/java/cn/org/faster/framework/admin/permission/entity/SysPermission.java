package cn.org.faster.framework.admin.permission.entity;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import cn.org.faster.framework.admin.common.entity.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 权限实体（自关联树，{@code parent_ids} 保存从根到父节点的完整路径）。
 *
 * @author zhangbowen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseEntity implements TreeNode {

    private String name;

    private String code;

    private Long parentId;

    /**
     * 父级路径，形如 {@code [0],[37615593091760128]}。
     */
    private String parentIds;

    private List<TreeNode> children;
}
