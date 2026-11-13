package cn.org.faster.framework.admin.permission.service;

import cn.org.faster.framework.admin.common.entity.TreeNode;
import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.common.util.AuditSupport;
import cn.org.faster.framework.admin.common.util.TreeUtils;
import cn.org.faster.framework.admin.permission.entity.SysPermission;
import cn.org.faster.framework.admin.permission.error.SysPermissionError;
import cn.org.faster.framework.admin.permission.model.SysPermissionAddReq;
import cn.org.faster.framework.admin.permission.model.SysPermissionUpdateReq;
import cn.org.faster.framework.admin.permission.repository.SysPermissionRepository;
import cn.org.faster.framework.admin.rolePermission.service.SysRolePermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限管理服务。
 *
 * @author zhangbowen
 */
@Service
@Transactional
public class SysPermissionService {

    private static final Comparator<TreeNode> PERMISSION_ORDER =
            Comparator.comparingInt((TreeNode node) -> sortOf((SysPermission) node))
                    .thenComparing(TreeNode::getId);

    private final SysPermissionRepository permissionRepository;

    private final SysRolePermissionService rolePermissionService;

    private final PermissionService permissionService;

    private final AuditSupport auditSupport;

    private final Clock clock;

    public SysPermissionService(SysPermissionRepository permissionRepository,
                                SysRolePermissionService rolePermissionService,
                                PermissionService permissionService,
                                AuditSupport auditSupport, Clock clock) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
        this.auditSupport = auditSupport;
        this.clock = clock;
    }

    /**
     * @return 权限树
     */
    @Transactional(readOnly = true)
    public List<TreeNode> tree() {
        return TreeUtils.build(permissionRepository.findAll(), PERMISSION_ORDER);
    }

    /**
     * @return 权限平铺列表
     */
    @Transactional(readOnly = true)
    public List<SysPermission> list() {
        return permissionRepository.findAll();
    }

    /**
     * @param permissionId 权限 id
     * @return 权限详情
     */
    @Transactional(readOnly = true)
    public SysPermission info(long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> BusinessException.notFound(SysPermissionError.NOT_EXIST));
    }

    /**
     * 新增权限。
     *
     * @param request 请求参数
     */
    public void add(SysPermissionAddReq request) {
        SysPermission permission = new SysPermission();
        permission.setName(request.name());
        permission.setCode(request.code());
        permission.setParentId(request.parentId());
        permission.setParentIds(resolveParentIds(request.parentId()));
        if (permissionRepository.existsCode(request.code(), null)) {
            throw BusinessException.badRequest(SysPermissionError.CODE_EXIST);
        }
        auditSupport.markCreated(permission);
        permissionRepository.insert(permission);
        permissionService.evictAll();
    }

    /**
     * 修改权限（不允许修改上级）。
     *
     * @param permissionId 权限 id
     * @param request      请求参数
     */
    public void update(long permissionId, SysPermissionUpdateReq request) {
        SysPermission exist = info(permissionId);
        if (!exist.getCode().equals(request.code())
                && permissionRepository.existsCode(request.code(), permissionId)) {
            throw BusinessException.badRequest(SysPermissionError.CODE_EXIST);
        }
        SysPermission update = new SysPermission();
        update.setId(permissionId);
        update.setName(request.name());
        update.setCode(request.code());
        auditSupport.markUpdated(update);
        permissionRepository.update(update);
        permissionService.evictAll();
    }

    /**
     * 删除权限及其所有子孙权限。
     *
     * @param permissionIds 权限 id 列表
     */
    public void delete(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        Set<Long> deleteIds = new LinkedHashSet<>();
        for (Long permissionId : permissionIds) {
            if (permissionId == null) {
                continue;
            }
            deleteIds.add(permissionId);
            deleteIds.addAll(permissionRepository.findSubtreeIds(permissionId));
        }
        LocalDateTime now = LocalDateTime.now(clock);
        permissionRepository.softDelete(deleteIds, auditSupport.currentUserId(), now);
        rolePermissionService.deleteByPermissionIds(new ArrayList<>(deleteIds));
        permissionService.evictAll();
    }

    private String resolveParentIds(long parentId) {
        if (parentId == 0L) {
            return "[0]";
        }
        SysPermission parent = permissionRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(SysPermissionError.PARENT_NOT_EXIST, HttpStatus.BAD_REQUEST));
        return parent.getParentIds() + ",[" + parent.getId() + "]";
    }

    private static int sortOf(SysPermission permission) {
        return permission.getSort() == null ? 0 : permission.getSort();
    }
}
