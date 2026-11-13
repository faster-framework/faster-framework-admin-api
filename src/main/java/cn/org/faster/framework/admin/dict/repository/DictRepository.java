package cn.org.faster.framework.admin.dict.repository;

import cn.org.faster.framework.admin.dict.entity.SysDict;
import cn.org.faster.framework.admin.dict.model.DictQuery;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 字典表数据访问。
 *
 * @author faster-builder
 */
@Repository
public class DictRepository {

    private static final String COLUMNS = "id, name, type, dict_value, show_status, create_by, update_by, "
            + "create_date, update_date, sort, remark, deleted";

    private static final RowMapper<SysDict> ROW_MAPPER = new DataClassRowMapper<>(SysDict.class);

    private final JdbcClient jdbcClient;

    public DictRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SysDict> page(DictQuery query) {
        Filter filter = Filter.of(query, true);
        JdbcClient.StatementSpec spec = jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_dict"
                        + filter.where() + " ORDER BY sort, id LIMIT :limit OFFSET :offset")
                .params(filter.params())
                .param("limit", query.getSize())
                .param("offset", query.offset());
        return spec.query(ROW_MAPPER).list();
    }

    public long count(DictQuery query) {
        Filter filter = Filter.of(query, true);
        return jdbcClient.sql("SELECT COUNT(1) FROM sys_dict" + filter.where())
                .params(filter.params())
                .query(Long.class)
                .single();
    }

    public List<SysDict> findAll() {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_dict WHERE deleted = 0 ORDER BY sort, id")
                .query(ROW_MAPPER)
                .list();
    }

    public Optional<SysDict> findById(long id) {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_dict WHERE id = :id AND deleted = 0")
                .param("id", id)
                .query(ROW_MAPPER)
                .optional();
    }

    /**
     * 按条件查询首条记录（旧实现使用 getOne，多结果时会抛异常，此处改为首条）。
     */
    public Optional<SysDict> findFirst(DictQuery query) {
        Filter filter = Filter.of(query, false);
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM sys_dict" + filter.where()
                        + " ORDER BY sort, id LIMIT 1")
                .params(filter.params())
                .query(ROW_MAPPER)
                .optional();
    }

    public void insert(SysDict dict) {
        jdbcClient.sql("INSERT INTO sys_dict (id, name, type, dict_value, show_status, create_by, update_by, "
                        + "create_date, update_date, sort, remark, deleted) "
                        + "VALUES (:id, :name, :type, :dictValue, :showStatus, :createBy, :updateBy, "
                        + ":createDate, :updateDate, :sort, :remark, 0)")
                .param("id", dict.getId())
                .param("name", dict.getName())
                .param("type", dict.getType())
                .param("dictValue", dict.getDictValue())
                .param("showStatus", dict.getShowStatus() == null ? 1 : dict.getShowStatus())
                .param("createBy", dict.getCreateBy(), Types.BIGINT)
                .param("updateBy", dict.getUpdateBy(), Types.BIGINT)
                .param("createDate", dict.getCreateDate())
                .param("updateDate", dict.getUpdateDate())
                .param("sort", dict.getSort() == null ? 0 : dict.getSort())
                .param("remark", dict.getRemark(), Types.VARCHAR)
                .update();
    }

    public int update(SysDict dict) {
        return jdbcClient.sql("UPDATE sys_dict SET name = :name, type = :type, dict_value = :dictValue, "
                        + "show_status = :showStatus, remark = :remark, update_by = :updateBy, "
                        + "update_date = :updateDate WHERE id = :id AND deleted = 0")
                .param("name", dict.getName())
                .param("type", dict.getType())
                .param("dictValue", dict.getDictValue())
                .param("showStatus", dict.getShowStatus() == null ? 1 : dict.getShowStatus())
                .param("remark", dict.getRemark(), Types.VARCHAR)
                .param("updateBy", dict.getUpdateBy(), Types.BIGINT)
                .param("updateDate", dict.getUpdateDate())
                .param("id", dict.getId())
                .update();
    }

    public int softDelete(Collection<Long> ids, Long operator, LocalDateTime now) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return jdbcClient.sql("UPDATE sys_dict SET deleted = 1, update_by = :updateBy, update_date = :updateDate "
                        + "WHERE id IN (:ids) AND deleted = 0")
                .param("updateBy", operator, Types.BIGINT)
                .param("updateDate", now)
                .param("ids", ids)
                .update();
    }

    /**
     * 条件拼装：分页查询使用右模糊，单条精确查询使用等值。
     */
    private record Filter(String where, java.util.Map<String, Object> params) {

        static Filter of(DictQuery query, boolean fuzzy) {
            StringBuilder where = new StringBuilder(" WHERE deleted = 0");
            List<String> conditions = new ArrayList<>();
            java.util.Map<String, Object> params = new java.util.LinkedHashMap<>();
            if (StringUtils.hasText(query.getName())) {
                conditions.add("name LIKE :name");
                params.put("name", fuzzy ? query.getName() + "%" : query.getName());
            }
            if (StringUtils.hasText(query.getType())) {
                conditions.add("type LIKE :type");
                params.put("type", fuzzy ? query.getType() + "%" : query.getType());
            }
            if (StringUtils.hasText(query.getRemark())) {
                conditions.add("remark LIKE :remark");
                params.put("remark", fuzzy ? query.getRemark() + "%" : query.getRemark());
            }
            if (StringUtils.hasText(query.getDictValue())) {
                conditions.add("dict_value = :dictValue");
                params.put("dictValue", query.getDictValue());
            }
            if (query.getShowStatus() != null) {
                conditions.add("show_status = :showStatus");
                params.put("showStatus", query.getShowStatus());
            }
            if (!conditions.isEmpty()) {
                where.append(" AND ").append(String.join(" AND ", conditions));
            }
            return new Filter(where.toString(), params);
        }
    }
}
