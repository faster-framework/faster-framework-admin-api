package cn.org.faster.framework.admin.common.entity;

import java.util.List;

/**
 * 树形节点契约，用于权限树等自关联结构。
 *
 * @author zhangbowen
 */
public interface TreeNode {

    Long getId();

    Long getParentId();

    List<TreeNode> getChildren();

    void setChildren(List<TreeNode> children);
}
