package cn.org.faster.framework.admin.user.service;

import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.common.model.PageResult;
import cn.org.faster.framework.admin.common.util.AuditSupport;
import cn.org.faster.framework.admin.permission.service.PermissionService;
import cn.org.faster.framework.admin.security.AuthProperties;
import cn.org.faster.framework.admin.security.TokenRegistry;
import cn.org.faster.framework.admin.user.entity.SysUser;
import cn.org.faster.framework.admin.user.error.UserError;
import cn.org.faster.framework.admin.user.model.SysUserAddReq;
import cn.org.faster.framework.admin.user.model.SysUserChangePwdReq;
import cn.org.faster.framework.admin.user.model.SysUserUpdateReq;
import cn.org.faster.framework.admin.user.model.UserQuery;
import cn.org.faster.framework.admin.user.repository.SysUserRepository;
import cn.org.faster.framework.admin.userRole.entity.SysUserRole;
import cn.org.faster.framework.admin.userRole.service.SysUserRoleService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理服务。
 *
 * @author zhangbowen
 */
@Service
@Transactional
public class SysUserService {

    /**
     * 超级管理员用户 id。
     */
    public static final long ADMIN_USER_ID = 0L;

    private final SysUserRepository userRepository;

    private final SysUserRoleService userRoleService;

    private final PermissionService permissionService;

    private final PasswordEncoder passwordEncoder;

    private final AuthProperties authProperties;

    private final TokenRegistry tokenRegistry;

    private final AuditSupport auditSupport;

    private final Clock clock;

    public SysUserService(SysUserRepository userRepository, SysUserRoleService userRoleService,
                          PermissionService permissionService, PasswordEncoder passwordEncoder,
                          AuthProperties authProperties, TokenRegistry tokenRegistry,
                          AuditSupport auditSupport, Clock clock) {
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.tokenRegistry = tokenRegistry;
        this.auditSupport = auditSupport;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResult<SysUser> list(UserQuery query) {
        List<SysUser> records = userRepository.page(query.getName(), query.getSize(), query.offset());
        long total = userRepository.count(query.getName());
        return PageResult.of(records, total, query);
    }

    @Transactional(readOnly = true)
    public SysUser info(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound(UserError.USER_NOT_EXIST));
    }

    public void add(SysUserAddReq request) {
        if (userRepository.existsAccount(request.account(), null)) {
            throw BusinessException.conflict(UserError.USER_EXIST);
        }
        SysUser user = new SysUser();
        user.setAccount(request.account());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        auditSupport.markCreated(user);
        userRepository.insert(user);
    }

    public void update(long userId, SysUserUpdateReq request) {
        info(userId);
        userRepository.updateName(userId, request.name(), auditSupport.currentUserId(), LocalDateTime.now(clock));
    }

    /**
     * 批量删除用户（超级管理员不可删除）。
     *
     * @param userIds 用户 id 列表
     */
    public void delete(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            if (userId != null && userId == ADMIN_USER_ID) {
                throw BusinessException.badRequest(UserError.ADMIN_CANNOT_DELETE);
            }
        }
        List<Long> deletable = userIds.stream().filter(java.util.Objects::nonNull).toList();
        userRepository.softDelete(deletable, auditSupport.currentUserId(), LocalDateTime.now(clock));
        for (Long userId : deletable) {
            userRoleService.deleteByUserId(userId);
            tokenRegistry.revokeAll(userId);
            permissionService.evict(userId);
        }
    }

    /**
     * 用户自行修改密码：校验旧密码，修改后强制所有令牌失效。
     */
    public void changePassword(long userId, SysUserChangePwdReq request) {
        SysUser user = info(userId);
        if (!passwordEncoder.matches(request.oldPwd(), user.getPassword())) {
            throw BusinessException.badRequest(UserError.OLD_PASSWORD_ERROR);
        }
        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.badRequest(UserError.PASSWORD_NOT_CHANGED);
        }
        userRepository.updatePassword(userId, passwordEncoder.encode(request.password()),
                auditSupport.currentUserId(), LocalDateTime.now(clock));
        tokenRegistry.revokeAll(userId);
    }

    /**
     * 管理员重置密码为配置的初始口令，并强制该用户所有令牌失效。
     */
    public void resetPassword(long userId) {
        info(userId);
        userRepository.updatePassword(userId, passwordEncoder.encode(authProperties.defaultPassword()),
                auditSupport.currentUserId(), LocalDateTime.now(clock));
        tokenRegistry.revokeAll(userId);
    }

    @Transactional(readOnly = true)
    public List<Long> roles(long userId) {
        return userRoleService.roles(userId);
    }

    public void chooseRoles(long userId, List<SysUserRole> roles) {
        info(userId);
        userRoleService.batchChoose(userId, roles);
    }
}
