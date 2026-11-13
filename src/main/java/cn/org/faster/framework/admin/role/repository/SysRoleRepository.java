package cn.org.faster.framework.admin.role.repository;

import cn.org.faster.framework.admin.role.entity.SysRole;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 角色表数据访问。
 *
 * @author zhangbowen
 */
@Repository
public class SysRoleRepository {

    private static final String COLUMNS = "id, name, create_by, update_by, create_date, update_date, "
            + "sort, remark, deleted";

    private static final RowMapper<SysRole> ROW_MAPPER = new DataClassRowMapper<>(SysRole.class);

    private final JdbcClient jdbcClient;

    public SysRoleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SysRole> page(String name, int limit, long offset) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM sys_role WHERE deleted = 0");
        if (StringUtils.hasText(name)) {
            sql.append(" AND name LIKE :name");
        }
        sql.append(" ORDER BY create_date DESC, id DESC LIMIT :limit OFFSET :offset");
        JdbcClient.StatementSpec spec = jdbcClient.sql(sql.toString())
                .param("limit", limit)
                .param("offset", offset);
        if (StringUtils.hasText(name)) {
            spec = spec.param("name", name + "%");
        }
        return spec.query(ROW_MAPPER).list();
    }

    public long count(String name) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM sys_role WHERE deleted = 0");
        if (StringUtils.hasText(name)) {
            sql.append(" AND name LIKE :name");
        }
        JdbcClient.StatementSpec spec = jdbcClient.sql(sql.toString());
        if (StringUtils.hasText(name)) {
            spec = spec.param("name", name + "%");
        }
        return spec.query(Long.class).single();
    }

    public Optional<SysRole> findById(long id) {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_role WHERE id = :id AND deleted = 0")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    public void insert(SysRole role) {
        jdbcClient.sql("INSERT INTO sys_role (id, name, create_by, update_by, create_date, update_date, "
                        + "sort, remark, deleted) "
                        + "VALUES (:id, :name, :createBy, :updateBy, :createDate, :updateDate, :sort, :remark, 0)")
                .param("id", role.getId())
                .param("name", role.getName())
                .param("createBy", role.getCreateBy(), Types.BIGINT)
                .param("updateBy", role.getUpdateBy(), Types.BIGINT)
                .param("createDate", role.getCreateDate())
                .param("updateDate", role.getUpdateDate())
                .param("sort", role.getSort() == null ? 0 : role.getSort())
                .param("remark", role.getRemark(), Types.VARCHAR)
                .update();
    }

    public int update(SysRole role) {
        return jdbcClient.sql("UPDATE sys_role SET name = :name, update_by = :updateBy, update_date = :updateDate "
                        + "WHERE id = :id AND deleted = 0")
                .param("name", role.getName())
                .param("updateBy", role.getUpdateBy(), Types.BIGINT)
                .param("updateDate", role.getUpdateDate())
                .param("id", role.getId())
                .update();
    }

    public int softDelete(Collection<Long> ids, Long operator, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return jdbcClient.sql("UPDATE sys_role SET deleted = 1, update_by = :updateBy, update_date = :updateDate "
                        + "WHERE id IN (:ids) AND deleted = 0")
                .param("updateBy", operator, Types.BIGINT)
                .param("updateDate", now)
                .param("ids", ids)
                .update();
    }
}
