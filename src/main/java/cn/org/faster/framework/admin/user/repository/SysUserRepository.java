package cn.org.faster.framework.admin.user.repository;

import cn.org.faster.framework.admin.user.entity.SysUser;
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
 * 用户表数据访问。
 *
 * @author zhangbowen
 */
@Repository
public class SysUserRepository {

    private static final String COLUMNS = "id, account, password, name, create_by, update_by, "
            + "create_date, update_date, sort, remark, deleted";

    private static final RowMapper<SysUser> ROW_MAPPER = new DataClassRowMapper<>(SysUser.class);

    private final JdbcClient jdbcClient;

    public SysUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SysUser> page(String name, int limit, long offset) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM sys_user WHERE deleted = 0");
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM sys_user WHERE deleted = 0");
        if (StringUtils.hasText(name)) {
            sql.append(" AND name LIKE :name");
        }
        JdbcClient.StatementSpec spec = jdbcClient.sql(sql.toString());
        if (StringUtils.hasText(name)) {
            spec = spec.param("name", name + "%");
        }
        return spec.query(Long.class).single();
    }

    public Optional<SysUser> findById(long id) {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_user WHERE id = :id AND deleted = 0")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    public Optional<SysUser> findByAccount(String account) {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_user WHERE account = :account AND deleted = 0")
                .param("account", account)
                .query(ROW_MAPPER)
                .optional();
    }

    public boolean existsAccount(String account, Long excludeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM sys_user WHERE account = :account AND deleted = 0");
        if (excludeId != null) {
            sql.append(" AND id <> :excludeId");
        }
        JdbcClient.StatementSpec spec = jdbcClient.sql(sql.toString()).param("account", account);
        if (excludeId != null) {
            spec = spec.param("excludeId", excludeId);
        }
        return spec.query(Long.class).single() > 0;
    }

    public void insert(SysUser user) {
        jdbcClient.sql("INSERT INTO sys_user (id, account, password, name, create_by, update_by, "
                        + "create_date, update_date, sort, remark, deleted) "
                        + "VALUES (:id, :account, :password, :name, :createBy, :updateBy, "
                        + ":createDate, :updateDate, :sort, :remark, 0)")
                .param("id", user.getId())
                .param("account", user.getAccount())
                .param("password", user.getPassword())
                .param("name", user.getName())
                .param("createBy", user.getCreateBy(), Types.BIGINT)
                .param("updateBy", user.getUpdateBy(), Types.BIGINT)
                .param("createDate", user.getCreateDate())
                .param("updateDate", user.getUpdateDate())
                .param("sort", user.getSort() == null ? 0 : user.getSort())
                .param("remark", user.getRemark(), Types.VARCHAR)
                .update();
    }

    public int updateName(long id, String name, Long operator, LocalDateTime now) {
        return jdbcClient.sql("UPDATE sys_user SET name = :name, update_by = :updateBy, update_date = :updateDate "
                        + "WHERE id = :id AND deleted = 0")
                .param("name", name)
                .param("updateBy", operator, Types.BIGINT)
                .param("updateDate", now)
                .param("id", id)
                .update();
    }

    public int updatePassword(long id, String password, Long operator, LocalDateTime now) {
        return jdbcClient.sql("UPDATE sys_user SET password = :password, update_by = :updateBy, "
                        + "update_date = :updateDate WHERE id = :id AND deleted = 0")
                .param("password", password)
                .param("updateBy", operator, Types.BIGINT)
                .param("updateDate", now)
                .param("id", id)
                .update();
    }

    public int softDelete(Collection<Long> ids, Long operator, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return jdbcClient.sql("UPDATE sys_user SET deleted = 1, update_by = :updateBy, update_date = :updateDate "
                        + "WHERE id IN (:ids) AND deleted = 0")
                .param("updateBy", operator, Types.BIGINT)
                .param("updateDate", now)
                .param("ids", ids)
                .update();
    }
}
