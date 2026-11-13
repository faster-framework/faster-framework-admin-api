package cn.org.faster.framework.admin.userRole.repository;

import cn.org.faster.framework.admin.common.util.IdGenerator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户-角色关系数据访问。
 *
 * <p>关系表采用物理删除：每次“选择角色”都是全量覆盖，
 * 物理删除可以避免逻辑删除行无限累积（旧实现的主要缺陷之一）。</p>
 *
 * @author zhangbowen
 */
@Repository
public class SysUserRoleRepository {

    private final JdbcClient jdbcClient;

    private final IdGenerator idGenerator;

    public SysUserRoleRepository(JdbcClient jdbcClient, IdGenerator idGenerator) {
        this.jdbcClient = jdbcClient;
        this.idGenerator = idGenerator;
    }

    public List<Long> findRoleIds(long userId) {
        return jdbcClient.sql("SELECT role_id FROM sys_user_role WHERE user_id = :userId AND deleted = 0")
                .param("userId", userId)
                .query(Long.class)
                .list();
    }

    public void replace(long userId, Collection<Long> roleIds, Long operator, LocalDateTime now) {
        deleteByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            jdbcClient.sql("INSERT INTO sys_user_role (id, user_id, role_id, create_by, update_by, "
                            + "create_date, update_date, sort, deleted) "
                            + "VALUES (:id, :userId, :roleId, :operator, :operator, :now, :now, 0, 0)")
                    .param("id", idGenerator.next())
                    .param("userId", userId)
                    .param("roleId", roleId)
                    .param("operator", operator, java.sql.Types.BIGINT)
                    .param("now", now)
                    .update();
        }
    }

    public void deleteByUserId(long userId) {
        jdbcClient.sql("DELETE FROM sys_user_role WHERE user_id = :userId")
                .param("userId", userId)
                .update();
    }

    public void deleteByRoleId(long roleId) {
        jdbcClient.sql("DELETE FROM sys_user_role WHERE role_id = :roleId")
                .param("roleId", roleId)
                .update();
    }

    public long countByRoleId(long roleId) {
        return jdbcClient.sql("SELECT COUNT(1) FROM sys_user_role WHERE role_id = :roleId AND deleted = 0")
                .param("roleId", roleId)
                .query(Long.class)
                .single();
    }
}
