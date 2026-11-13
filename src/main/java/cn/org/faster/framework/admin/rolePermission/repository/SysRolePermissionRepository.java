package cn.org.faster.framework.admin.rolePermission.repository;

import cn.org.faster.framework.admin.common.util.IdGenerator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 角色-权限关系数据访问。
 *
 * @author zhangbowen
 */
@Repository
public class SysRolePermissionRepository {

    private final JdbcClient jdbcClient;

    private final IdGenerator idGenerator;

    public SysRolePermissionRepository(JdbcClient jdbcClient, IdGenerator idGenerator) {
        this.jdbcClient = jdbcClient;
        this.idGenerator = idGenerator;
    }

    public List<Long> findPermissionIds(long roleId) {
        return jdbcClient.sql("SELECT permission_id FROM sys_role_permission "
                        + "WHERE role_id = :roleId AND deleted = 0 ORDER BY permission_id")
                .param("roleId", roleId)
                .query(Long.class)
                .list();
    }

    public void replace(long roleId, Collection<Long> permissionIds, Long operator, LocalDateTime now) {
        deleteByRoleId(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long permissionId : permissionIds) {
            jdbcClient.sql("INSERT INTO sys_role_permission (id, role_id, permission_id, create_by, update_by, "
                            + "create_date, update_date, sort, deleted) "
                            + "VALUES (:id, :roleId, :permissionId, :operator, :operator, :now, :now, 0, 0)")
                    .param("id", idGenerator.next())
                    .param("roleId", roleId)
                    .param("permissionId", permissionId)
                    .param("operator", operator, java.sql.Types.BIGINT)
                    .param("now", now)
                    .update();
        }
    }

    public void deleteByRoleId(long roleId) {
        jdbcClient.sql("DELETE FROM sys_role_permission WHERE role_id = :roleId")
                .param("roleId", roleId)
                .update();
    }

    public void deleteByPermissionIds(Collection<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        jdbcClient.sql("DELETE FROM sys_role_permission WHERE permission_id IN (:permissionIds)")
                .param("permissionIds", permissionIds)
                .update();
    }
}
