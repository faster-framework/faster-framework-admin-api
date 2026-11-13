package cn.org.faster.framework.admin.permission.repository;

import cn.org.faster.framework.admin.permission.entity.SysPermission;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 权限表数据访问（Spring JDBC 7 {@link JdbcClient}）。
 *
 * @author zhangbowen
 */
@Repository
public class SysPermissionRepository {

    private static final String COLUMNS = "id, name, code, parent_id, parent_ids, create_by, update_by, "
            + "create_date, update_date, sort, remark, deleted";

    private static final RowMapper<SysPermission> ROW_MAPPER = new DataClassRowMapper<>(SysPermission.class);

    private final JdbcClient jdbcClient;

    public SysPermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SysPermission> findAll() {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_permission WHERE deleted = 0 "
                        + "ORDER BY parent_ids, sort, id")
                .query(ROW_MAPPER)
                .list();
    }

    public Optional<SysPermission> findById(long id) {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_permission WHERE id = :id AND deleted = 0")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    public List<SysPermission> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_permission "
                        + "WHERE deleted = 0 AND id IN (:ids)")
                .param("ids", ids)
                .query(ROW_MAPPER)
                .list();
    }

    public List<Long> findAllIds() {
        return jdbcClient.sql("SELECT id FROM sys_permission WHERE deleted = 0 ORDER BY sort, id")
                .query(Long.class)
                .list();
    }

    public List<String> findAllCodes() {
        return jdbcClient.sql("SELECT code FROM sys_permission WHERE deleted = 0 AND code IS NOT NULL")
                .query(String.class)
                .list();
    }

    public List<String> findCodesByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("SELECT DISTINCT p.code FROM sys_permission p "
                        + "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id AND rp.deleted = 0 "
                        + "WHERE p.deleted = 0 AND p.code IS NOT NULL AND rp.role_id IN (:roleIds)")
                .param("roleIds", roleIds)
                .query(String.class)
                .list();
    }

    /**
     * 查询某权限及其全部子孙权限 id。
     */
    public List<Long> findSubtreeIds(long id) {
        return jdbcClient.sql("SELECT id FROM sys_permission WHERE deleted = 0 AND parent_ids LIKE :pattern")
                .param("pattern", "%[" + id + "]%")
                .query(Long.class)
                .list();
    }

    public boolean existsCode(String code, Long excludeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM sys_permission WHERE deleted = 0 AND code = :code");
        if (excludeId != null) {
            sql.append(" AND id <> :excludeId");
        }
        JdbcClient.StatementSpec spec = jdbcClient.sql(sql.toString()).param("code", code);
        if (excludeId != null) {
            spec = spec.param("excludeId", excludeId);
        }
        return spec.query(Long.class).single() > 0;
    }

    public void insert(SysPermission permission) {
        jdbcClient.sql("INSERT INTO sys_permission (id, name, code, parent_id, parent_ids, create_by, update_by, "
                        + "create_date, update_date, sort, remark, deleted) "
                        + "VALUES (:id, :name, :code, :parentId, :parentIds, :createBy, :updateBy, "
                        + ":createDate, :updateDate, :sort, :remark, 0)")
                .param("id", permission.getId())
                .param("name", permission.getName())
                .param("code", permission.getCode())
                .param("parentId", permission.getParentId())
                .param("parentIds", permission.getParentIds())
                .param("createBy", permission.getCreateBy(), Types.BIGINT)
                .param("updateBy", permission.getUpdateBy(), Types.BIGINT)
                .param("createDate", permission.getCreateDate())
                .param("updateDate", permission.getUpdateDate())
                .param("sort", permission.getSort() == null ? 0 : permission.getSort())
                .param("remark", permission.getRemark(), Types.VARCHAR)
                .update();
    }

    public int update(SysPermission permission) {
        return jdbcClient.sql("UPDATE sys_permission SET name = :name, code = :code, update_by = :updateBy, "
                        + "update_date = :updateDate WHERE id = :id AND deleted = 0")
                .param("name", permission.getName())
                .param("code", permission.getCode())
                .param("updateBy", permission.getUpdateBy(), Types.BIGINT)
                .param("updateDate", permission.getUpdateDate())
                .param("id", permission.getId())
                .update();
    }

    public int softDelete(Collection<Long> ids, Long operator, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return jdbcClient.sql("UPDATE sys_permission SET deleted = 1, update_by = :updateBy, update_date = :updateDate "
                        + "WHERE id IN (:ids) AND deleted = 0")
                .param("updateBy", operator, Types.BIGINT)
                .param("updateDate", now)
                .param("ids", ids)
                .update();
    }
}
