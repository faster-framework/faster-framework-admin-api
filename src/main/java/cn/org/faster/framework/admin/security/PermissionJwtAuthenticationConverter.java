package cn.org.faster.framework.admin.security;

import cn.org.faster.framework.admin.permission.service.PermissionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 把 JWT 转换为 Spring Security 认证对象，并实时装配 RBAC 权限。
 *
 * <p>与旧版 Shiro Realm 不同，权限不写入令牌，而是每次请求从
 * {@link PermissionService}（带短时缓存）读取，因此角色/权限调整可以立即生效。</p>
 *
 * @author zhangbowen
 */
@Component
public class PermissionJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PermissionService permissionService;

    public PermissionJwtAuthenticationConverter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        long userId = Long.parseLong(jwt.getSubject());
        List<GrantedAuthority> authorities = permissionService.codesOf(userId).stream()
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
