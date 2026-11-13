package cn.org.faster.framework.admin.dict.entity;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典实体。
 *
 * @author faster-builder
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDict extends BaseEntity {

    /**
     * 字典 key。
     */
    private String name;

    /**
     * 字典类型。
     */
    private String type;

    /**
     * 字典值（JSON 文本）。
     */
    private String dictValue;

    /**
     * 展示状态：0 不展示，1 展示。
     */
    private Integer showStatus;
}
