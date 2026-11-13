package cn.org.faster.framework.admin.auth.controller;

import cn.org.faster.framework.admin.auth.model.LoginReq;
import cn.org.faster.framework.admin.auth.model.LoginRes;
import cn.org.faster.framework.admin.auth.model.UserInfoRes;
import cn.org.faster.framework.admin.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 *
 * @author zhangbowen
 */
@RestController
@RequestMapping("/admin")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginRes login(@Valid @RequestBody LoginReq request) {
        return authService.login(request);
    }

    @GetMapping("/userInfo")
    public UserInfoRes userInfo(Authentication authentication) {
        return authService.userInfo(authentication);
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) {
        authService.logout(authentication);
    }
}
