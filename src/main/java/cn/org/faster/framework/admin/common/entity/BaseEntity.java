package cn.org.faster.framework.admin.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 所有持久化实体的公共字段。
 *
 * <p>字段与数据库列一一对应，采用下划线列名 + 驼峰属性名，由
 * {@code DataClassRowMapper} 自动映射。</p>
 *
 * @author zhangbowen
 */
@Data
public abstract class BaseEntity {

    /**
     * 主键（雪花算法生成；超级管理员用户/角色固定为 0）。
     */
    private Long id;

    /**
     * 创建人 id。
     */
    private Long createBy;

    /**
     * 最后更新人 id。
     */
    private Long updateBy;

    /**
     * 创建时间。
     */
    private LocalDateTime createDate;

    /**
     * 最后更新时间。
     */
    private LocalDateTime updateDate;

    /**
     * 排序值，升序。
     */
    private Integer sort;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 逻辑删除标记：0 未删除，1 已删除。
     */
    private Integer deleted;
}
