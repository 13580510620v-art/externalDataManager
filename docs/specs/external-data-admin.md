# 外部数据管理平台 Web 管理端规格

## 1. 目标与范围

本规格定义外部数据管理系统 Web 管理端的全栈实现。系统用于管理供应商配置、外部数据文件任务、当日运行概览、用户、群组与权限，并支持用户名/密码登录和可配置的 SAML2 单点登录。

### 必须实现

- 登录、退出、当前用户信息与权限查询。
- 用户名/密码登录和 Spring Security SAML2 可配置登录。
- 两种登录方式统一使用服务端托管的安全 Token。
- 文件任务分页查询、筛选、详情和失败任务重试。
- 供应商分页查询、创建、编辑、启用与禁用。
- Dashboard 展示当日任务汇总、状态分布和供应商分布。
- 用户、群组、权限管理及后端授权。
- 简体中文、繁体中文、英文三种界面语言。
- 审计日志记录高风险操作。
- MySQL Flyway 数据库结构与必要种子数据。

### 暂不实现

- SFTP 实际拉取。
- S3 实际上传。
- 下游系统通知发送。
- 下游确认回执接收。
- 传输调度引擎。

失败任务重试仅表示任务重新进入待处理队列，等待后续传输引擎消费。

## 2. 技术栈

### 后端

- Java 17。
- Spring Boot 3.5.6。
- Spring Security。
- MyBatis-Plus `3.5.12`。
- Flyway。
- MySQL 8。
- Redis 8 与 Redisson。
- springdoc-openapi `2.8.17`，提供 OpenAPI 3 与 Swagger UI。
- Maven Wrapper。

### 前端

- Node.js 18.12.1。
- Vite `^4.5.x`，禁止 Vite 5。
- Vue `^3.5.x`。
- TypeScript `^5.6.x`。
- vue-tsc `^3.2.x`。
- vue-router `^4.6.x`。
- Pinia `^2.2.x`。
- Element Plus `^2.9.x`。
- Tailwind CSS `^3.4.x`。
- PostCSS `^8.4.x`。
- Autoprefixer `^10.4.x`。
- Axios `^1.7.x`。
- VueUse `^14.x`。
- vue-i18n `^9.x`。
- lucide-vue-next 图标。

## 3. 数据模型

所有表名和字段名使用大写驼峰转下划线后的大写形式。数据库字符集为 `utf8mb4`，引擎为 InnoDB。

### 3.1 供应商表

沿用需求给定的 `EXTERNAL_SUPPLIER` 结构，并增加以下约束：

- `SUPPLIER_CODE` 唯一且不能为空。
- `SUPPLIER_NAME` 不能为空。
- `FETCHER_TYPE` 只允许 `SFTP`、`REST`。
- `IS_ENABLE` 只允许 `0`、`1`。
- `UPDATE_FREQUENCY` 必须大于等于 1。
- `S3_BUCKET`、`FILE_NAME_RULE` 可为空。

### 3.2 数据任务表

沿用需求给定的 `DATA_TASK` 结构，并增加以下约束：

- `SOURCE_UNIQUE_KEY` 唯一且不能为空。
- `SOURCE_FILE_NAME`、`STATUS`、`FETCHER_TYPE` 不能为空。
- `FETCHER_TYPE` 只允许 `SFTP`、`REST`。
- `FEEDBACK_FLAG` 只允许 `Y`、`N`。
- 重试次数字段必须大于等于 0。

任务状态固定如下：

| 状态 | 含义 |
|---|---|
| `PENDING` | 待处理 |
| `DOWNLOADING` | 下载中 |
| `DOWNLOAD_FAILED` | 下载失败 |
| `UPLOADED` | 已上传 S3 |
| `UPLOAD_FAILED` | 上传失败 |
| `INFORMED` | 已通知下游 |
| `INFORM_FAILED` | 通知失败 |
| `COMPLETED` | 下游已确认 |

### 3.3 用户表

表名：`SYS_USER`

字段：

- `ID BIGINT PRIMARY KEY AUTO_INCREMENT`
- `USERNAME VARCHAR(64) NOT NULL UNIQUE`
- `PASSWORD_HASH VARCHAR(255) NULL`
- `FULL_NAME VARCHAR(100) NOT NULL`
- `EMAIL VARCHAR(255) NOT NULL UNIQUE`
- `SAML_NAME_ID VARCHAR(255) NULL UNIQUE`
- `IS_ENABLE TINYINT(1) NOT NULL DEFAULT 1`
- `CREATE_TIME DATETIME NOT NULL`
- `UPDATE_TIME DATETIME NOT NULL`

密码登录用户必须有 `PASSWORD_HASH`；SAML 用户可以没有本地密码。

### 3.4 群组表

表名：`SYS_GROUP`

字段：

- `ID BIGINT PRIMARY KEY AUTO_INCREMENT`
- `GROUP_CODE VARCHAR(64) NOT NULL UNIQUE`
- `GROUP_NAME VARCHAR(100) NOT NULL`
- `DESCRIPTION VARCHAR(512) NULL`
- `IS_ENABLE TINYINT(1) NOT NULL DEFAULT 1`
- `CREATE_TIME DATETIME NOT NULL`
- `UPDATE_TIME DATETIME NOT NULL`

### 3.5 权限表

表名：`SYS_PERMISSION`

字段：

- `ID BIGINT PRIMARY KEY AUTO_INCREMENT`
- `PERMISSION_CODE VARCHAR(100) NOT NULL UNIQUE`
- `PERMISSION_NAME VARCHAR(100) NOT NULL`
- `RESOURCE_TYPE VARCHAR(50) NOT NULL`
- `ACTION VARCHAR(50) NOT NULL`
- `IS_ENABLE TINYINT(1) NOT NULL DEFAULT 1`

### 3.6 关联表

- `SYS_USER_GROUP`：`USER_ID`、`GROUP_ID`，联合唯一。
- `SYS_GROUP_PERMISSION`：`GROUP_ID`、`PERMISSION_ID`，联合唯一。

均使用外键指向对应主键，并设置 `ON DELETE CASCADE`。

### 3.7 审计日志表

表名：`AUDIT_LOG`

字段：

- `ID BIGINT PRIMARY KEY AUTO_INCREMENT`
- `OPERATOR VARCHAR(64) NOT NULL`
- `ACTION VARCHAR(64) NOT NULL`
- `TARGET_TYPE VARCHAR(50) NOT NULL`
- `TARGET_ID VARCHAR(64) NULL`
- `DETAIL VARCHAR(1000) NULL`
- `CREATE_TIME DATETIME NOT NULL`

## 4. 认证与授权

### 4.1 Token 与跨域设计

- 登录成功后生成 256 位随机 opaque token。
- Token 明文只在 HTTP 响应 Set-Cookie 中出现一次，不落库、不写日志。
- Redis 中只保存 Token 的 SHA-256 哈希。
- Cookie 名称：`EDM_TOKEN`。
- Cookie 属性：`HttpOnly`、`Secure`、`Path=/`。
- 同域部署时使用 `SameSite=Lax`；跨域部署时必须使用 `SameSite=None`，且 `Secure=true`。
- 会话有效期可配置，默认 8 小时。
- 退出登录立即删除 Redis 会话并清除 Cookie。
- 后端 CORS 只允许显式配置的来源，禁止使用 `*`。
- 跨域请求必须开启 `Access-Control-Allow-Credentials`。
- 前端 axios 必须设置 `withCredentials: true`。
- 跨域或 Cookie 认证下的写接口必须启用 CSRF 防护，采用 Spring Security Cookie CSRF Token 与 `X-XSRF-TOKEN` 请求头。
- CORS 允许方法仅包含 `GET`、`POST`。
- 开发环境优先使用 Vite 同域代理；生产环境通过 `EDM_CORS_ALLOWED_ORIGINS` 配置跨域白名单。

### 4.2 登录方式

- 用户名/密码登录使用 `PasswordEncoder` 校验 BCrypt 哈希。
- SAML2 通过 Spring Security Relying Party 配置接入。
- IdP 未配置时，后端返回 SAML 未启用信息，前端隐藏或禁用 SSO 按钮。
- SAML 用户首次登录时，如果配置允许自动开户，则按 `SAML_NAME_ID` 创建用户；否则返回需要管理员预建用户的业务错误。

### 4.3 权限编码

- `dashboard:read`
- `supplier:read`
- `supplier:write`
- `task:read`
- `task:retry`
- `user:read`
- `user:write`
- `group:read`
- `group:write`
- `permission:read`
- `permission:write`

授权必须在后端 service 层执行，前端只做展示层隐藏，不作为安全边界。

## 5. API 设计

所有响应使用统一 `ApiResponse`：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 5.0 HTTP 方法规则

- 系统自定义 API 只使用 `GET` 和 `POST`。
- `GET` 仅用于无副作用的查询。
- 创建、更新、启用、禁用、重试、权限分配等所有变更操作必须使用 `POST`。
- 不使用 `PUT`、`PATCH`、`DELETE`。
- Spring Security SAML 协议端点按 SAML2 标准保留必要的 `GET`、`POST` 方法。

### 5.1 认证

- `POST /api/auth/login`：用户名/密码登录。
- `GET /api/auth/saml/enabled`：查询 SAML 是否启用。
- `GET /saml2/authenticate`：Spring Security SAML 登录入口。
- `POST /api/auth/logout`：退出。
- `GET /api/auth/me`：当前用户和权限。

### 5.2 供应商

- `GET /api/suppliers`
- `POST /api/suppliers`
- `POST /api/suppliers/{id}/update`
- `POST /api/suppliers/{id}/enable`
- `POST /api/suppliers/{id}/disable`

查询参数：`keyword`、`fetcherType`、`enabled`、`page`、`size`。

### 5.3 数据任务

- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/tasks/{id}/retry`

查询参数：`supplierCode`、`status`、`fileName`、`feedbackFlag`、`startTime`、`endTime`、`page`、`size`。

重试规则：

- 只允许 `DOWNLOAD_FAILED`、`UPLOAD_FAILED`、`INFORM_FAILED`。
- 根据状态分别累计 `DOWNLOAD_RETRY_TIMES`、`UPDATE_RETRY_TIMES`、`INFORM_RETRY_TIMES`。
- 状态重置为 `PENDING`。
- 更新审计人、审计时间。
- 写入审计日志。

### 5.4 Dashboard

- `GET /api/dashboard/today`

返回：

- 当日任务总数。
- 各状态数量。
- 各供应商数量。
- 成功率。
- 失败数。
- 待处理数。

### 5.5 系统管理

- `GET /api/users`
- `POST /api/users`
- `POST /api/users/{id}/update`
- `POST /api/users/{id}/enable`
- `POST /api/users/{id}/disable`
- `GET /api/groups`
- `POST /api/groups`
- `POST /api/groups/{id}/update`
- `GET /api/permissions`
- `POST /api/groups/{id}/permissions`

用户更新密码时必须由后端使用 `PasswordEncoder` 哈希，不允许前端提交哈希。

## 6. Swagger 3 / OpenAPI 3

### 6.1 实现要求

- 使用 `springdoc-openapi-starter-webmvc-ui`，禁止使用 Springfox。
- OpenAPI 描述文件遵循 OpenAPI 3 规范。
- API 文档路径：`/v3/api-docs`。
- 分组文档路径：`/v3/api-docs/{group}`，管理端分组固定为 `management`。
- Swagger UI 路径：`/swagger-ui.html` 与 `/swagger-ui/**`。
- 文档标题：`外部数据管理平台 Web 管理端 API`。
- 文档版本与应用版本一致，默认 `1.0.0`。
- 所有接口、请求参数、响应字段和错误响应必须有中文说明。
- 所有自定义接口只允许出现 `get` 和 `post` 方法。
- 控制器使用 `io.swagger.v3.oas.annotations` 下的 `@Tag`、`@Operation`、`@Parameter`、`@SecurityRequirement`。
- DTO 使用 `@Schema` 描述字段含义、必填性和示例值。
- 不使用 Springfox 注解。

### 6.2 安全方案

OpenAPI 3 安全方案固定为：

- `CookieAuth`：`type: apiKey`，位置 `cookie`，名称 `EDM_TOKEN`。
- `CsrfHeader`：`type: apiKey`，位置 `header`，名称 `X-XSRF-TOKEN`。

登录接口不添加安全要求；其余业务接口同时声明这两个安全方案。Swagger UI 开启 CSRF 支持，通过 `springdoc.swagger-ui.csrf.enabled=true` 读取 Cookie CSRF Token 并发送 `X-XSRF-TOKEN`。

### 6.3 开关与访问控制

`application.yml` 配置固定如下：

```yaml
springdoc:
  api-docs:
    enabled: ${EDM_SWAGGER_ENABLED:false}
    path: /v3/api-docs
  swagger-ui:
    enabled: ${EDM_SWAGGER_ENABLED:false}
    path: /swagger-ui.html
    with-credentials: true
    csrf:
      enabled: true
```

- `EDM_SWAGGER_ENABLED=false` 时：
  - `/v3/api-docs/**` 返回不可用。
  - `/swagger-ui/**` 与 `/swagger-ui.html` 返回不可用。
  - Spring Security 不放行 Swagger 路径。
- `EDM_SWAGGER_ENABLED=true` 时：
  - Spring Security 放行 `/v3/api-docs/**`、`/swagger-ui/**`、`/swagger-ui.html`。
  - Swagger 路径复用后端 CORS 白名单。
  - Swagger UI 请求携带 Cookie 凭证。
  - 生产环境默认必须关闭。

Swagger 文档不得展示密码哈希、Token 哈希、数据库连接串、Redis 地址或其他内部敏感配置。

## 7. 前端页面

### 7.1 布局

- 左侧固定侧边栏。
- 顶部 `h-14` 导航栏。
- 内容区 `bg-background`、`p-6`。
- 所有颜色使用 `theme.css` Token 和 Tailwind 扩展类。
- 表格窄屏使用横向滚动。
- 操作列使用清晰按钮，高风险操作必须确认。

### 7.2 页面

- `/login`：登录页，支持账号密码和 SSO。
- `/dashboard`：当日汇总、状态卡片、供应商分布。
- `/tasks`：任务查询、筛选、详情、重试。
- `/suppliers`：供应商查询和管理。
- `/system/users`：用户管理。
- `/system/groups`：群组管理。
- `/system/permissions`：权限管理。

### 7.3 多语言

- 语言编码：`zh-CN`、`zh-TW`、`en`。
- 顶栏提供语言切换。
- 语言偏好写入 `localStorage`。
- 页面文案、菜单、按钮、表格表头、状态和错误信息均需翻译。

## 8. 安全要求

- 不向 API 返回密码哈希、Token 哈希或内部堆栈。
- 业务错误统一走 `BusinessException` 和 `ErrorCode`。
- Swagger 仅通过配置开启，生产默认关闭。
- 登录失败返回统一错误，不区分用户不存在和密码错误。
- 高风险操作写审计日志。
- 后端 service 层执行权限校验。
- 跨站请求防护使用 Cookie CSRF Token，写接口必须校验 `X-XSRF-TOKEN`。

## 9. 验证

### 后端

- 认证与 Token 安全测试。
- CORS、CSRF 和 HTTP 方法约束测试。
- Swagger 3 / OpenAPI 3 文档、安全方案和开关测试。
- 权限校验测试。
- 供应商业务规则测试。
- 任务查询和重试测试。
- Dashboard 聚合测试。
- 用户、群组、权限管理测试。

### 前端

- `npm run build`。
- Axios Cookie、CSRF 请求头和跨域配置测试。
- 路由守卫和权限展示人工检查。
- 三种语言切换检查。
- 登录页、Dashboard、任务、供应商、系统管理页面构建检查。

## 10. 配置

必要环境变量：

- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `JWT_SECRET`：至少 32 bytes，用于 SAML 依赖包要求的密钥配置。
- `EDM_ADMIN_USERNAME`
- `EDM_ADMIN_PASSWORD`
- `EDM_SAML_ENABLED`
- `EDM_SAML_IDP_METADATA_URL`
- `EDM_SAML_ENTITY_ID`
- `EDM_CORS_ALLOWED_ORIGINS`：逗号分隔的显式来源白名单。
- `EDM_COOKIE_SAMESITE`：`LAX` 或 `NONE`，默认 `LAX`。
- `EDM_API_BASE_URL`：前端访问后端 API 的基础地址；同域部署可留空。
- `EDM_SWAGGER_ENABLED`：是否启用 OpenAPI 文档与 Swagger UI，默认 `false`。

开发环境提供 `docker-compose.yml` 启动 MySQL 与 Redis。
