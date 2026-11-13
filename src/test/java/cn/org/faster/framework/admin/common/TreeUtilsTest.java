package cn.org.faster.framework.admin.common;

import cn.org.faster.framework.admin.common.entity.TreeNode;
import cn.org.faster.framework.admin.common.util.TreeUtils;
import cn.org.faster.framework.admin.permission.entity.SysPermission;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 权限树构造单元测试。
 *
 * @author zhangbowen
 */
class TreeUtilsTest {

    private static final Comparator<TreeNode> ORDER = Comparator.comparing(TreeNode::getId);

    @Test
    void buildsTreeEvenWhenChildrenComeBeforeParents() {
        List<SysPermission> nodes = List.of(node(2, 1), node(3, 1), node(1, 0));

        List<TreeNode> roots = TreeUtils.build(nodes, ORDER);

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getId()).isEqualTo(1L);
        assertThat(roots.get(0).getChildren()).extracting(TreeNode::getId).containsExactly(2L, 3L);
        assertThat(roots.get(0).getChildren().get(0).getChildren()).isEmpty();
    }

    @Test
    void keepsNodesWhoseParentIsMissing() {
        List<SysPermission> nodes = List.of(node(1, 0), node(5, 999));

        List<TreeNode> roots = TreeUtils.build(nodes, ORDER);

        assertThat(roots).extracting(TreeNode::getId).containsExactly(1L, 5L);
    }

    @Test
    void toleratesCyclicParentReferences() {
        List<SysPermission> nodes = List.of(node(1, 2), node(2, 1));

        List<TreeNode> roots = TreeUtils.build(nodes, ORDER);

        assertThat(roots).extracting(TreeNode::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void sortsSiblingsByComparator() {
        List<SysPermission> nodes = List.of(node(10, 0), node(20, 0), node(30, 0));

        List<TreeNode> roots = TreeUtils.build(nodes, ORDER.reversed());

        assertThat(roots).extracting(TreeNode::getId).containsExactly(30L, 20L, 10L);
    }

    private SysPermission node(long id, long parentId) {
        SysPermission permission = new SysPermission();
        permission.setId(id);
        permission.setParentId(parentId);
        permission.setSort(0);
        return permission;
    }
}
