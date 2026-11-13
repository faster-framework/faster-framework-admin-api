# 迁移说明（1.x → 2.0）

本文件记录本次「全项目焕新」的变更范围、替换关系与**不兼容点**，供前端联调与运维升级参考。

## 1. 为什么必须重写框架层

旧版 `pom.xml` 直接继承私有父 POM：

```xml
<parent>
  <groupId>cn.org.faster</groupId>
  <artifactId>spring-boot-starters</artifactId>
  <version>1.3.8.RELEASE</version>
</parent>
```

该 starter 体系构建于 **Spring Boot 2.1.5 / Spring Framework 5.1 / JDK 8**，与 Spring Boot 4 的自动配置模型（`AutoConfiguration.imports`、Jakarta EE 11、Jackson 3、Spring Security 7）不兼容，且没有对应 4.x 版本。因此 2.0 将原先由 starter 提供的能力**在仓库内原生实现**，并移除了 `cn.org.faster` 依赖。

## 2. 技术栈替换对照

| 旧实现 | 新实现 | 说明 |
|---|---|---|
| Spring Boot 2.1.5 / Spring 5.1 | Spring Boot 4.1.2 / Spring Framework 7.0.10 | 父 POM 改为 `spring-boot-starter-parent:4.1.2`（其管理的 Spring 版本恰为 7.0.10） |
| JDK 8 | JDK 17 | 使用 record、text block、`var`、`instanceof` 模式匹配、`List.of/Set.of`、`Stream.toList()` |
| `javax.validation` | `jakarta.validation`（Bean Validation 3.1） | 注解包名与实现（Hibernate Validator 9）同步升级 |
| Apache Shiro 1.4 | Spring Security 7.1.2（OAuth2 Resource Server + `@EnableMethodSecurity`） | `@RequiresPermissions` → `@PreAuthorize("hasAuthority('...')")` |
| jjwt 0.9.0 | Spring Security `JwtEncoder` / `JwtDecoder`（Nimbus JOSE，HS256） | 令牌带 `iss/sub/iat/exp/jti`，支持撤销与单端登录 |
| MyBatis-Plus 3.1.0 + `BaseMapper`/`LambdaQueryWrapper` | Spring JDBC 7 `JdbcClient` + 显式 SQL | 删除 `mapper` 包，改为 `repository`；SQL 全部使用命名参数，无字符串拼接 |
| Druid 1.1.10 | HikariCP 7.0.2 | Spring Boot 默认连接池 |
| patchca 0.0.1 | 自研 AWT 验证码（`CaptchaService`） | Base64 PNG，进程内一次性校验，60 秒过期 |
| FastJSON / Hutool / Guava | 移除 | 统一使用 Jackson 3 + JDK 标准库 |
| `META-INF/spring.factories` 自动装配 | 可执行应用（`AdminApplication`） | 仓库定位为「应用」而非 starter；`spring-boot-maven-plugin` 可正常打包 |
| Jackson 2（`com.fasterxml.jackson.databind`） | Jackson 3（`tools.jackson.databind`） | 注解仍为 `com.fasterxml.jackson.annotation`；`Long → String` 通过 `JacksonModule` Bean 注册 |
| Lombok 1.16.20 | Lombok 1.18.48（Boot 统一管理） | 兼容 JDK 17 |

## 3. 行为与接口契约变化

路径、请求体字段与权限码保持兼容；下列变化需要前端配合：

### 3.1 HTTP 状态码统一

| 场景 | 旧行为 | 新行为 |
|---|---|---|
| 登录：验证码错误 | 404 | 400 |
| 登录：账号/密码错误 | 404（1100/1101 可枚举用户） | 401（统一返回 1101「账号或密码错误」） |
| 参数校验失败 | 部分接口未校验 | 400（1001） |
| 无权限 | 由 Shiro 处理 | 403（1006） |
| 未登录/令牌失效 | 401（1002） | 401（1002，结构不变） |
| 新增资源 | 201 | 201 |
| 修改资源 | 201 | 200 |
| 删除资源 | 用户 201 / 角色 204 混用 | 204 |

### 3.2 响应体变化

- **用户列表/详情不再返回 `password`**（`@JsonIgnore`）。
- `GET /admin/userInfo` 新增 `id`、`account` 字段，`permissions` 仍为权限码数组。
- 分页响应仍为 `{records,total,size,current,pages}`；`size` 被限制在 1~100。
- 上传接口返回 `{fileName,url}`；预上传返回 `{token,expiresAt}`（原框架返回结构未文档化）。
- 错误体结构不变：`{timestamp,status,path,code,message}`。

### 3.3 错误码

| 变化 | 说明 |
|---|---|
| 1004 / 1008 / 1009 | 新增：资源不存在 / 数据冲突 / 方法不支持 |
| 1100 | 保留但登录不再返回（防用户名枚举），前端可继续映射 |
| 1101 | 文案改为「账号或密码错误」 |
| 1112 / 1121 / 1134 | 新增：权限不存在 / 角色不存在 / 新密码与旧密码相同 |
| 1150~1153 | 新增：上传相关错误 |

### 3.4 认证与令牌

- 令牌**默认 2 小时过期**（旧版为永不过期），可通过 `app.jwt.ttl` 调整。
- `DELETE /admin/logout` 现在会**撤销服务端令牌**（旧版仅清缓存）。
- 修改密码 / 重置密码 / 删除用户会**使该用户全部已签发令牌失效**。
- `app.auth.multipart-terminal=false` 时启用单端登录（旧版该开关因缺陷必然失败）。
- 新增支持 `Authorization: Bearer <token>`（仍兼容 `Auth-Token` 与 `auth-token` Cookie）。
- 验证码校验改为**一次性消费**，60 秒过期，不可重放。
- `app.captcha.enabled=false` 可关闭验证码校验，**仅供本地联调与自动化测试**。

### 3.5 授权

- 权限不再写入令牌，也不再有「授权缓存按 principal 整体清空」的问题：每次请求读取带 TTL 的权限缓存，角色/权限/用户角色变更时即时清空。
- 权限树构造不再依赖输入顺序，父节点缺失或存在环时不会丢节点。

### 3.6 已修复的旧版缺陷（对应内部分析报告）

| 编号 | 问题 | 处理 |
|---|---|---|
| P0-1 | 无启动类、无配置 | 新增 `AdminApplication` + `application.yml` |
| P0-2 | 用户接口回显密码哈希 | `@JsonIgnore` |
| P0-3 | Token 永不过期、登出无效 | 过期时间 + jti 撤销 |
| P0-4 | 单端登录必然失败 | 令牌登记表 + 校验器 |
| P0-5 | 无盐 MD5、固定弱口令 | BCrypt；默认口令可配置 |
| P1-3 | 批量赋值 | 请求 DTO 全部改为 record，不再继承 `BaseEntity` |
| P1-4 | 分页无上限 | `PageQuery` 钳制 1~100 |
| P1-5 | 批量删除无事务 | Service 层统一 `@Transactional` |
| P1-6 | 授权装配 N+1 | 单条 JOIN 查询 + 缓存 |
| P1-9 | 唯一性无约束 | 账号、关系表联合唯一索引 |
| P1-11 | `getOne` 多结果 500 | 改为 `LIMIT 1` |
| P1-12 | HTTP 语义不一致 | 统一状态码 |
| P2-4 | Service 误用 `@PathVariable` | 已移除 |
| P2-11 | 权限树依赖排序 | `TreeUtils` 重写 |

## 4. 数据库变更

`db/faster-admin.sql` 已重写（MySQL 8）：

- `sys_user.password` 由 `varchar(64)` 扩为 `varchar(100)`（BCrypt 哈希 + `{bcrypt}` 前缀，68 字符）。
- 新增唯一约束：`uk_sys_user_account(account)`、`uk_sys_user_role(user_id, role_id)`、`uk_sys_role_permission(role_id, permission_id)`。
- `sys_permission.parent_ids` 由 `longtext` 改为 `varchar(2048)`（可索引、跨库可移植）。
- `sys_dict.dict_value` 由 `longtext` 改为 `text`。
- 新增索引：`idx_sys_permission_parent`、`idx_sys_permission_code`、`idx_sys_role_name`、`idx_sys_dict_type`、`idx_sys_dict_name`。
- 内置管理员密码更新为 BCrypt 哈希；权限种子数据保持 31 条，修正「用户选择权限」文案为「角色选择权限」。
- 关系表（`sys_user_role`、`sys_role_permission`）在代码中改为**物理删除**，重复「选择」不会累积逻辑删除行。

**升级已有库时**请手工执行等价 `ALTER TABLE`（或新建库后导入数据），并重置所有用户密码为 BCrypt 值。

## 5. 构建与运行变化

- 构建命令：`mvn clean package`（原父 POM 中的 `skipTests=true` 已移除，测试默认执行）。
- 运行：`java -jar target/faster-framework-admin-api-2.0.0.jar`。
- 版本号由 `1.0-SNAPSHOT` 升为 `2.0.0`。
- 仓库不再作为 starter 被其它工程引入；如仍需复用，请将 `admin` 包作为普通依赖并在宿主工程 `@ComponentScan`。

## 6. 已知限制

- `TokenRegistry`、`CaptchaService`、`PermissionCache` 为进程内实现，集群部署需替换为 Redis（类的方法签名已按可替换方式设计）。
- 未实现登录失败锁定、审计日志、刷新令牌、OpenAPI 文档。
- 文件上传按扩展名白名单校验，未做文件内容（magic number）检测。
- 超级管理员仍以 `id = 0` 约定，未改为可配置角色标识。

## 7. 验证结果

| 项目 | 结果 |
|---|---|
| 环境 | JDK 17.0.20.1（Temurin 兼容版本）、Maven 3.6.3 |
| 编译 | `mvn clean package` 成功 |
| 测试 | 56 个用例全部通过（集成测试 31 + 单元测试 25） |
| 打包 | 生成可执行 fat jar（约 30 MB） |
