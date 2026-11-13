# Faster-Admin-API

Faster-Admin 的后端 API，一套「开箱即用」的后台管理接口：用户、角色、权限（RBAC）、数据字典、图形验证码、文件上传，全部基于当前主流版本重新实现。

> 本仓库已完成技术栈全面焕新：**JDK 17 + Spring Boot 4.1.2 + Spring Framework 7.0.10**。
> 旧版依赖的 `cn.org.faster:*:1.3.8.RELEASE` 私有 starter（基于 Spring Boot 2.1 + Shiro + MyBatis-Plus）已移除，
> 由此带来的行为变化见 [MIGRATION.md](MIGRATION.md)。

## 技术栈

| 分类 | 组件 | 版本 |
|---|---|---|
| 语言 / 构建 | Java / Maven | 17 / 3.6.3+ |
| 基础框架 | Spring Boot / Spring Framework | 4.1.2 / 7.0.10 |
| Web | Spring MVC（`spring-boot-starter-webmvc`）+ 内嵌 Tomcat | 7.0.10 / 11.0.27 |
| 安全 | Spring Security + OAuth2 Resource Server（Nimbus JOSE，HS256 JWT） | 7.1.2 |
| 持久层 | Spring JDBC `JdbcClient` + HikariCP（无 ORM） | 7.0.10 / 7.0.2 |
| 校验 | Jakarta Bean Validation（`jakarta.validation`） | 3.1 |
| JSON | Jackson 3 | 3.1.7 |
| 数据库 | MySQL 8.x（生产） / H2 2.4（测试） | — |
| 测试 | JUnit 6 + MockMvc + AssertJ + Mockito | 6.0.3 |

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < db/faster-admin.sql
```

脚本会创建 `faster_admin` 库、6 张表与内置管理员（`admin / admin`，BCrypt 存储）。

### 2. 配置

| 环境变量 | 说明 | 默认值 |
|---|---|---|
| `DB_URL` | JDBC 连接串 | `jdbc:mysql://127.0.0.1:3306/faster_admin?...` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | `root` / 空 |
| `APP_JWT_SECRET` | JWT HMAC 密钥（Base64，解码后 ≥ 32 字节），**生产必须替换** | 内置开发密钥 |
| `APP_UPLOAD_DIR` | 上传文件目录 | `./data/upload` |
| `SERVER_PORT` | 端口 | `8080` |

完整配置见 [`application.yml`](src/main/resources/application.yml)。

### 3. 启动

```bash
mvn clean package
java -jar target/faster-framework-admin-api-2.0.0.jar
```

### 4. 登录

```bash
# ① 取图形验证码（token + Base64 PNG）
curl http://localhost:8080/admin/captcha

# ② 登录，得到 JWT
curl -X POST http://localhost:8080/admin/login \
  -H 'Content-Type: application/json' \
  -d '{"account":"admin","password":"admin","captcha":"<图片中的字符>","captchaToken":"<上一步的 token>"}'

# ③ 携带令牌访问
curl http://localhost:8080/admin/userInfo -H "Auth-Token: <token>"
```

令牌支持三种传递方式：`Auth-Token` 请求头（兼容旧前端）、`Authorization: Bearer <token>`、`auth-token` Cookie。

## 接口一览

所有接口统一前缀 `/admin`，错误响应统一为：

```json
{ "timestamp": 1700000000000, "status": 401, "path": "/admin/sys/users", "code": 1002, "message": "登录状态过期" }
```

| 方法 | 路径 | 权限码 | 说明 | 成功状态 |
|---|---|---|---|---|
| GET | `/admin/captcha` | 匿名 | 图形验证码 | 200 |
| GET | `/admin/captcha/valid` | 匿名 | 校验验证码 | 200 |
| POST | `/admin/login` | 匿名 | 登录 | 200 |
| GET | `/admin/userInfo` | 登录 | 当前用户信息 | 200 |
| DELETE | `/admin/logout` | 登录 | 登出（撤销令牌） | 204 |
| GET | `/admin/sys/users` | `users:list` | 用户分页 | 200 |
| GET | `/admin/sys/users/{id}` | `users:info` | 用户详情 | 200 |
| POST | `/admin/sys/users` | `users:add` | 新增用户 | 201 |
| PUT | `/admin/sys/users/{id}` | `users:modify` | 修改姓名 | 200 |
| DELETE | `/admin/sys/users/delete` | `users:delete` | 批量删除 | 204 |
| PUT | `/admin/sys/users/{id}/password/change` | `users:password:change` | 修改密码 | 204 |
| PUT | `/admin/sys/users/{id}/password/reset` | `users:password:reset` | 重置密码 | 204 |
| PUT | `/admin/sys/users/{id}/roles/choose` | `users:roles:choose` | 分配角色 | 204 |
| GET | `/admin/sys/users/{id}/roles` | `users:roles:list` | 角色 id 列表 | 200 |
| GET | `/admin/sys/roles` | `roles:list` | 角色分页 | 200 |
| GET | `/admin/sys/roles/{id}` | `roles:info` | 角色详情 | 200 |
| POST | `/admin/sys/roles` | `roles:add` | 新增角色 | 201 |
| PUT | `/admin/sys/roles/{id}` | `roles:modify` | 修改角色 | 200 |
| DELETE | `/admin/sys/roles/delete` | `roles:delete` | 批量删除 | 204 |
| PUT | `/admin/sys/roles/{id}/permissions/choose` | `roles:permissions:choose` | 分配权限 | 204 |
| GET | `/admin/sys/roles/{id}/permissions` | `roles:permissions:list` | 权限 id 列表 | 200 |
| GET | `/admin/sys/permissions/tree` | `permissions:tree` | 权限树 | 200 |
| GET | `/admin/sys/permissions` | `permissions:list` | 权限列表 | 200 |
| GET | `/admin/sys/permissions/{id}` | `permissions:info` | 权限详情 | 200 |
| POST | `/admin/sys/permissions` | `permissions:add` | 新增权限 | 201 |
| PUT | `/admin/sys/permissions/{id}` | `permissions:modify` | 修改权限 | 200 |
| DELETE | `/admin/sys/permissions/delete` | `permissions:delete` | 批量删除（级联子树） | 204 |
| GET | `/admin/sys/dict/all` | 登录 | 字典全量列表 | 200 |
| GET | `/admin/sys/dict` | `dict:list` | 字典分页 | 200 |
| GET | `/admin/sys/dict/{id}` | `dict:info` | 字典详情 | 200 |
| GET | `/admin/sys/dict/query` | `dict:info` | 条件查单条 | 200 |
| POST | `/admin/sys/dict` | `dict:add` | 新增字典 | 201 |
| PUT | `/admin/sys/dict/{id}` | `dict:modify` | 修改字典 | 200 |
| DELETE | `/admin/sys/dict/delete` | `dict:delete` | 批量删除 | 204 |
| GET | `/admin/upload/preload` | 登录 | 获取上传凭证 | 200 |
| POST | `/admin/upload` | 登录 | 上传文件 | 201 |
| GET | `/admin/media/{fileName}/_download` | 匿名 | 下载文件 | 200 |

错误码分段：`1000~1009` 通用、`1100~1102` 认证、`1110~1112` 权限、`1120~1121` 角色、`1130~1134` 用户、`1140` 角色权限、`1150~1153` 上传。

## 目录结构

```
src/main/java/cn/org/faster/framework/admin
├── AdminApplication.java           启动类
├── common/                         公共能力：实体基类、分页、错误模型、id、树工具、Jackson 配置
├── security/                       Spring Security 7 配置、JWT 签发/校验、令牌登记表
├── auth/                           登录、登出、当前用户
├── captcha/                        图形验证码（AWT 生成 PNG，进程内一次性校验）
├── upload/ + media/                本地上传与下载
├── user/ role/ permission/          RBAC 领域（controller/entity/model/repository/service/error）
├── userRole/ rolePermission/        多对多关系维护
└── dict/                           数据字典
```

分层约定：`controller` 只做参数与权限注解，`service` 承担事务与业务规则，`repository` 使用 `JdbcClient` 编写显式 SQL，`entity` 与表一一对应，`model` 中的请求对象一律使用 record 且不再继承实体。

## 测试

```bash
mvn test
```

- 集成测试：`@SpringBootTest` + MockMvc + H2（`MODE=MySQL`），覆盖登录/登出、RBAC 即时生效、用户生命周期、字典、上传下载、统一错误契约；测试运行在事务中并自动回滚。
- 单元测试：令牌签发与校验、令牌登记表、验证码、雪花 id、分页、权限树。
- 测试环境通过 `application-test.yml` 关闭验证码（`app.captcha.enabled=false`），生产环境请保持开启。

## 安全设计要点

- 密码使用 BCrypt（`DelegatingPasswordEncoder`，`{bcrypt}` 前缀），接口永不输出 `password` 字段。
- JWT 带 `exp`（默认 2 小时）与 `jti`；登出撤销当前令牌，改密/重置密码/删除用户会使该用户全部令牌失效。
- 可选单端登录：`app.auth.multipart-terminal=false` 时，后登录的令牌会顶掉先前的令牌。
- 权限不写入令牌，每次请求按需读取（带 5 分钟缓存，角色/权限变更即时清空），调整权限无需重新登录。
- 账号唯一索引、关系表联合唯一索引；分页 `size` 上限 100；上传白名单 + 大小限制 + 路径穿越防护。
- 生产环境务必通过 `APP_JWT_SECRET` 覆盖默认密钥，并修改 `admin` 默认密码。

## 已知限制

- 令牌登记表、验证码、权限缓存均为进程内实现，适用于单实例部署；集群部署请替换为 Redis（接口已抽象，见 `TokenRegistry` / `CaptchaService` / `PermissionCache`）。
- 超级管理员以 `id = 0` 约定（`sys_user.id`、`sys_role.id`），属于历史兼容设计。
- 未内置登录失败锁定与审计日志，可按需扩展。

## License

[Apache License 2.0](LICENSE)
