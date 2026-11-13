package cn.org.faster.framework.admin.auth.service;

import cn.org.faster.framework.admin.auth.error.AuthError;
import cn.org.faster.framework.admin.auth.model.LoginReq;
import cn.org.faster.framework.admin.auth.model.LoginRes;
import cn.org.faster.framework.admin.auth.model.UserInfoRes;
import cn.org.faster.framework.admin.captcha.service.CaptchaProperties;
import cn.org.faster.framework.admin.captcha.service.CaptchaService;
import cn.org.faster.framework.admin.common.error.BusinessException;
import cn.org.faster.framework.admin.permission.service.PermissionService;
import cn.org.faster.framework.admin.security.IssuedToken;
import cn.org.faster.framework.admin.security.TokenRegistry;
import cn.org.faster.framework.admin.security.TokenService;
import cn.org.faster.framework.admin.user.entity.SysUser;
import cn.org.faster.framework.admin.user.error.UserError;
import cn.org.faster.framework.admin.user.repository.SysUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务：登录、登出、当前用户信息。
 *
 * @author zhangbowen
 */
@Service
public class AuthService {

    private final SysUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final CaptchaService captchaService;

    private final CaptchaProperties captchaProperties;

    private final TokenService tokenService;

    private final TokenRegistry tokenRegistry;

    private final PermissionService permissionService;

    public AuthService(SysUserRepository userRepository, PasswordEncoder passwordEncoder,
                       CaptchaService captchaService, CaptchaProperties captchaProperties,
                       TokenService tokenService, TokenRegistry tokenRegistry,
                       PermissionService permissionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
        this.captchaProperties = captchaProperties;
        this.tokenService = tokenService;
        this.tokenRegistry = tokenRegistry;
        this.permissionService = permissionService;
    }

    /**
     * 登录：校验验证码 → 校验账号密码 → 签发 JWT。
     */
    @Transactional(readOnly = true)
    public LoginRes login(LoginReq request) {
        if (captchaProperties.enabled()
                && !captchaService.validate(request.captcha(), request.captchaToken())) {
            throw BusinessException.badRequest(AuthError.CAPTCHA_ERROR);
        }
        // 账号不存在与密码错误返回同一错误码，避免用户名枚举
        SysUser user = userRepository.findByAccount(request.account())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
                .orElseThrow(() -> BusinessException.unauthorized(AuthError.PASSWORD_ERROR));
        IssuedToken token = tokenService.issue(user.getId(), user.getName());
        return new LoginRes(token.value());
    }

    /**
     * 登出：撤销当前令牌并清理权限缓存。
     */
    public void logout(Authentication authentication) {
        Jwt jwt = jwtOf(authentication);
        if (jwt == null) {
            return;
        }
        long userId = Long.parseLong(jwt.getSubject());
        tokenRegistry.revoke(userId, jwt.getId(), jwt.getExpiresAt());
        permissionService.evict(userId);
    }

    /**
     * 当前登录用户信息。
     */
    @Transactional(readOnly = true)
    public UserInfoRes userInfo(Authentication authentication) {
        Jwt jwt = jwtOf(authentication);
        if (jwt == null) {
            throw BusinessException.unauthorized(UserError.USER_NOT_EXIST);
        }
        long userId = Long.parseLong(jwt.getSubject());
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound(UserError.USER_NOT_EXIST));
        return new UserInfoRes(user.getId(), user.getAccount(), user.getName(), permissionService.codesOf(userId));
    }

    private Jwt jwtOf(Authentication authentication) {
        return authentication instanceof JwtAuthenticationToken jwtAuthentication
                ? jwtAuthentication.getToken()
                : null;
    }
}
