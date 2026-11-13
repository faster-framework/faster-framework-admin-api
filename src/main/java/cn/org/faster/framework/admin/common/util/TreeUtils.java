package cn.org.faster.framework.admin.common.util;

import cn.org.faster.framework.admin.common.entity.TreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 树形结构工具。
 *
 * <p>与旧实现不同，本实现不依赖输入顺序，也不会因为父节点缺失而丢失节点：
 * 找不到父节点（或存在环）的节点会被当作根节点返回。</p>
 *
 * @author zhangbowen
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 将扁平节点集合转换为树。
     *
     * @param nodes      扁平节点
     * @param comparator 同级排序规则
     * @return 根节点列表
     */
    public static List<TreeNode> build(List<? extends TreeNode> nodes, Comparator<TreeNode> comparator) {
        Map<Long, TreeNode> index = new HashMap<>(nodes.size());
        for (TreeNode node : nodes) {
            if (node.getId() != null) {
                index.putIfAbsent(node.getId(), node);
            }
        }
        index.values().forEach(node -> node.setChildren(new ArrayList<>()));

        List<TreeNode> roots = new ArrayList<>();
        for (TreeNode node : index.values()) {
            TreeNode parent = node.getParentId() == null ? null : index.get(node.getParentId());
            if (parent == null || parent == node || isDescendant(index, parent, node)) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        roots.sort(comparator);
        roots.forEach(root -> sortChildren(root, comparator));
        return roots;
    }

    private static void sortChildren(TreeNode node, Comparator<TreeNode> comparator) {
        List<TreeNode> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        children.sort(comparator);
        children.forEach(child -> sortChildren(child, comparator));
    }

    /**
     * 判断 {@code candidate} 是否为 {@code node} 的后代，用于提前发现环。
     */
    private static boolean isDescendant(Map<Long, TreeNode> index, TreeNode candidate, TreeNode node) {
        Set<Long> visited = new HashSet<>();
        TreeNode current = candidate;
        while (current != null && visited.add(current.getId())) {
            if (current.getId().equals(node.getId())) {
                return true;
            }
            current = current.getParentId() == null ? null : index.get(current.getParentId());
        }
        return false;
    }
}
