package cn.org.faster.framework.admin.common.util;

import cn.org.faster.framework.admin.common.entity.BaseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 审计字段填充。
 *
 * <p>操作人取自 Spring Security 的安全上下文；匿名请求（如登录、种子数据）写入 null。</p>
 *
 * @author zhangbowen
 */
@Component
public class AuditSupport {

    private final IdGenerator idGenerator;

    private final Clock clock;

    public AuditSupport(IdGenerator idGenerator, Clock clock) {
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    /**
     * 填充新增实体：主键、审计字段、排序与逻辑删除标记。
     *
     * @param entity 实体
     */
    public void markCreated(BaseEntity entity) {
        LocalDateTime now = LocalDateTime.now(clock);
        Long operator = currentUserId();
        if (entity.getId() == null) {
            entity.setId(idGenerator.next());
        }
        entity.setCreateBy(operator);
        entity.setUpdateBy(operator);
        entity.setCreateDate(now);
        entity.setUpdateDate(now);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        entity.setDeleted(0);
    }

    /**
     * 填充更新实体。
     *
     * @param entity 实体
     */
    public void markUpdated(BaseEntity entity) {
        entity.setUpdateBy(currentUserId());
        entity.setUpdateDate(LocalDateTime.now(clock));
    }

    /**
     * @return 当前登录用户 id，未登录返回 null
     */
    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
