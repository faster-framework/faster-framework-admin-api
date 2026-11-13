package cn.org.faster.framework.admin.dict.model;

import cn.org.faster.framework.admin.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典查询条件。
 *
 * @author faster-builder
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictQuery extends PageQuery {

    private String name;

    private String type;

    private String dictValue;

    private String remark;

    private Integer showStatus;
}
