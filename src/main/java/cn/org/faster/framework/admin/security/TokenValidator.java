package cn.org.faster.framework.admin.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 令牌业务校验：登出撤销、改密失效、单端登录。
 *
 * <p>与时间戳/签发者的标准校验通过 {@code DelegatingOAuth2TokenValidator} 叠加使用。</p>
 *
 * @author zhangbowen
 */
@Component
public class TokenValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenRegistry tokenRegistry;

    private final AuthProperties authProperties;

    public TokenValidator(TokenRegistry tokenRegistry, AuthProperties authProperties) {
        this.tokenRegistry = tokenRegistry;
        this.authProperties = authProperties;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        long userId;
        try {
            userId = Long.parseLong(token.getSubject());
        } catch (NumberFormatException ex) {
            return failure("令牌 subject 非法");
        }
        String tokenId = token.getId();
        if (tokenRegistry.isRevoked(tokenId)) {
            return failure("令牌已登出");
        }
        Instant issuedAt = token.getIssuedAt();
        if (tokenRegistry.isIssuedBefore(userId, issuedAt)) {
            return failure("令牌已因密码变更失效");
        }
        if (authProperties.singleSession() && !tokenRegistry.isCurrent(userId, tokenId)) {
            return failure("令牌已被新的登录顶掉");
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
    }
}
