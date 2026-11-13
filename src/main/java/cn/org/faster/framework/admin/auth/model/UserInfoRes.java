package cn.org.faster.framework.admin.auth.model;

import java.util.Collection;

/**
 * 当前登录用户信息。
 *
 * @param id          用户 id
 * @param account     账号
 * @param name        姓名
 * @param permissions 权限编码集合
 * @author zhangbowen
 */
public record UserInfoRes(Long id, String account, String name, Collection<String> permissions) {
}
