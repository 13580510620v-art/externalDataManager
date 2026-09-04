# 外部数据管理平台 Web 管理端 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建可运行的外部数据管理系统全栈 Web 管理端，覆盖认证、权限、供应商、任务、Dashboard 和系统管理。

**Architecture:** 后端采用单体 Spring Boot 应用，使用 MyBatis-Plus、Flyway、Redis 和 Spring Security；认证统一签发服务端托管 opaque token，并在 service 层执行权限。前端采用 Vue 3、Vite 4、Element Plus 和 Tailwind Token 化设计系统，通过真实 API 访问所有数据。

**Tech Stack:** Java 17、Spring Boot 3.5.6、MySQL 8、Redis 8、Redisson、Vue 3.5、Vite 4.5、TypeScript 5.6、Element Plus 2.9、Tailwind 3.4。

**Spec:** `docs/specs/external-data-admin.md`

## Global Constraints

- 所有沟通和文档使用中文。
- Java 必须使用 17。
- Node.js 版本必须为 18.12.1。
- Vite 使用 `^4.5.x`，禁止 Vite 5。
- Vue 使用 `^3.5.x`。
- TypeScript 使用 `^5.6.x`。
- vue-tsc 使用 `^3.2.x`。
- Element Plus 使用 `^2.9.x`。
- Tailwind 使用 `^3.4.x`。
- API 响应结构保持与 `ApiResponse` 一致。
- 自定义 API 只使用 `GET` 和 `POST`，所有变更操作必须使用 `POST`。
- ORM 使用 MyBatis-Plus，不使用 Spring Data JPA。
- 跨域来源必须显式白名单，跨域 Cookie 认证必须启用 CSRF 防护。
- Swagger 3 使用 springdoc-openapi `2.8.17`，生产环境默认关闭。
- Swagger 注解统一使用 `io.swagger.v3.oas.annotations`，禁止 Springfox。
- 新增数据库结构只能追加 Flyway 迁移。
- 不提交 `frontend/dist`。
- 不使用前端 mock fallback。
- 后端 service 层执行授权。
- 高风险操作必须写审计日志。
- 未经用户明确要求不得提交 Git commit。

---

### Task 1: 后端工程骨架与数据库迁移

**Files:**

- Create: `backend/pom.xml`
- Create: `backend/mvnw`
- Create: `backend/mvnw.cmd`
- Create: `backend/src/main/java/com/edm/ExternalDataManagerApplication.java`
- Create: `backend/src/main/java/com/edm/config/MybatisPlusConfig.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql`
- Create: `backend/src/test/java/com/edm/ExternalDataManagerApplicationTests.java`
- Create: `docker-compose.yml`
- Create: `.gitignore`

**Interfaces:**

- Produces: Spring Boot 应用入口 `ExternalDataManagerApplication`。
- Produces: MyBatis-Plus 分页拦截器配置。
- Produces: 数据库表 `EXTERNAL_SUPPLIER`、`DATA_TASK`、`SYS_USER`、`SYS_GROUP`、`SYS_PERMISSION`、`SYS_USER_GROUP`、`SYS_GROUP_PERMISSION`、`AUDIT_LOG`。

- [ ] **Step 1: 创建后端失败测试**

在 `ExternalDataManagerApplicationTests` 中写入：

```java
package com.edm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExternalDataManagerApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: 验证测试因工程不存在而失败**

Run: `test -f backend/pom.xml`

Expected: 非零退出码。

- [ ] **Step 3: 创建 Maven 工程和应用入口**

`pom.xml` 使用 Java 17、Spring Boot 3.5.6、Web、Security、Validation、Redis、Flyway、MySQL、Redisson、MyBatis-Plus `3.5.12`、`springdoc-openapi-starter-webmvc-ui 2.8.17` 和测试依赖，不引入 Spring Data JPA 或 Springfox。`application.yml` 默认指向本地环境变量，不写入任何真实密码，并包含 `EDM_CORS_ALLOWED_ORIGINS`、Cookie `SameSite` 和 `EDM_SWAGGER_ENABLED` 配置。`MybatisPlusConfig` 注册 `PaginationInnerInterceptor`，并配置 MySQL 方言。

- [ ] **Step 4: 创建初始 Flyway 迁移**

迁移包含规格第 3 节所有表、索引、唯一键、外键和状态不变量。种子数据只包含权限编码和三个基础群组，不插入演示业务数据。

- [ ] **Step 5: 创建 Docker 开发依赖**

`docker-compose.yml` 提供 MySQL 8 与 Redis 8 服务，端口使用 `3306`、`6379`，卷持久化本地开发数据。

- [ ] **Step 6: 验证后端编译**

Run: `cd backend && ./mvnw test -Dtest=ExternalDataManagerApplicationTests`

Expected: PASS。如本机没有 MySQL/Redis，使用 Spring 上下文最小化测试配置并记录剩余风险。

---

### Task 2: 统一响应、异常与权限基础

**Files:**

- Create: `backend/src/main/java/com/edm/common/ApiResponse.java`
- Create: `backend/src/main/java/com/edm/common/PageResponse.java`
- Create: `backend/src/main/java/com/edm/exception/BusinessException.java`
- Create: `backend/src/main/java/com/edm/exception/ErrorCode.java`
- Create: `backend/src/main/java/com/edm/exception/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/edm/security/PermissionEvaluator.java`
- Test: `backend/src/test/java/com/edm/common/ApiResponseTest.java`
- Test: `backend/src/test/java/com/edm/security/PermissionEvaluatorTest.java`

**Interfaces:**

- Produces: `ApiResponse.success(T data)`。
- Produces: `ApiResponse.error(ErrorCode code, String message)`。
- Produces: `PageResponse.of(com.baomidou.mybatisplus.core.metadata.IPage<T> page)`。
- Produces: `PermissionEvaluator.require(String permissionCode)`。

- [ ] **Step 1: 写失败测试**

```java
package com.edm.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
    @Test
    void successUsesZeroCodeAndMessage() {
        ApiResponse<String> response = ApiResponse.success("ok");
        assertThat(response.getCode()).isZero();
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("ok");
    }
}
```

- [ ] **Step 2: 验证失败**

Run: `cd backend && ./mvnw test -Dtest=ApiResponseTest`

Expected: 编译失败，`ApiResponse` 不存在。

- [ ] **Step 3: 实现响应和权限基础**

实现统一响应、分页响应、业务异常、错误码、全局异常处理和权限校验器。权限校验从当前认证上下文读取权限集合，不通过则抛 `BusinessException`。

- [ ] **Step 4: 验证通过**

Run: `cd backend && ./mvnw test -Dtest=ApiResponseTest,PermissionEvaluatorTest`

Expected: PASS。

---

### Task 3: 认证与安全 Token

**Files:**

- Create: `backend/src/main/java/com/edm/security/AuthTokenService.java`
- Create: `backend/src/main/java/com/edm/security/LoginUser.java`
- Create: `backend/src/main/java/com/edm/security/TokenSession.java`
- Create: `backend/src/main/java/com/edm/security/TokenSessionStore.java`
- Create: `backend/src/main/java/com/edm/security/RedisTokenSessionStore.java`
- Create: `backend/src/main/java/com/edm/security/CurrentUser.java`
- Create: `backend/src/main/java/com/edm/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/edm/security/WebCorsConfig.java`
- Create: `backend/src/main/java/com/edm/auth/AuthController.java`
- Create: `backend/src/main/java/com/edm/auth/AuthService.java`
- Create: `backend/src/main/java/com/edm/auth/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/edm/auth/dto/CurrentUserResponse.java`
- Test: `backend/src/test/java/com/edm/auth/AuthTokenServiceTest.java`
- Test: `backend/src/test/java/com/edm/security/RedisTokenSessionStoreTest.java`
- Test: `backend/src/test/java/com/edm/security/WebCorsConfigTest.java`
- Test: `backend/src/test/java/com/edm/auth/AuthServiceTest.java`

**Interfaces:**

- Produces: `AuthTokenService.create(LoginUser user): TokenSession`，其中 `TokenSession` 包含明文 token、SHA-256 token 哈希和过期时间。
- Produces: `AuthTokenService.resolve(String token): LoginUser`，token 不存在时返回空。
- Produces: `TokenSessionStore.save(TokenSession session, LoginUser user)`。
- Produces: `TokenSessionStore.find(String tokenHash): LoginUser`。
- Produces: `TokenSessionStore.delete(String tokenHash)`。
- Produces: `AuthService.login(LoginRequest request): LoginUser`。
- Produces: `AuthService.currentUser(): CurrentUserResponse`。
- Produces: CORS 白名单、允许凭据、仅 `GET`/`POST` 方法的跨域配置。

- [ ] **Step 1: 写 Token 安全失败测试**

```java
package com.edm.auth;

import com.edm.security.AuthTokenService;
import com.edm.security.LoginUser;
import com.edm.security.TokenSession;
import com.edm.security.TokenSessionStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenServiceTest {
    private final Map<String, LoginUser> sessions = new HashMap<>();
    private final TokenSessionStore store = new TokenSessionStore() {
        @Override
        public void save(TokenSession session, LoginUser user) {
            sessions.put(session.tokenHash(), user);
        }

        @Override
        public Optional<LoginUser> find(String tokenHash) {
            return Optional.ofNullable(sessions.get(tokenHash));
        }

        @Override
        public void delete(String tokenHash) {
            sessions.remove(tokenHash);
        }
    };

    @Test
    void storesHashInsteadOfPlaintextToken() {
        AuthTokenService service = new AuthTokenService(store);
        LoginUser user = new LoginUser(1L, "admin", "ADMIN", java.util.Set.of("dashboard:read"));
        TokenSession session = service.create(user);

        assertThat(session.token()).isNotBlank();
        assertThat(session.token().getBytes()).hasSizeGreaterThanOrEqualTo(32);
        assertThat(session.tokenHash()).isNotEqualTo(session.token());
        assertThat(sessions).doesNotContainKey(session.token());
        assertThat(sessions).containsKey(session.tokenHash());
        assertThat(service.resolve(session.token())).contains(user);

        service.logout(session.token());
        assertThat(service.resolve(session.token())).isEmpty();
    }
}
```

测试同时断言 `TokenSession.expiresAt()` 为未来时间，Redis 实现写入 TTL 后由 `RedisTokenSessionStoreTest` 覆盖。

`WebCorsConfigTest` 必须断言：

- 未配置来源时不反射任意 `Origin`。
- 白名单来源返回 `Access-Control-Allow-Origin` 和 `Access-Control-Allow-Credentials: true`。
- 非白名单来源不返回允许凭证响应头。
- 预检允许方法只包含 `GET`、`POST`。
- 写接口缺少 `X-XSRF-TOKEN` 时返回 CSRF 错误。

- [ ] **Step 2: 验证失败**

Run: `cd backend && ./mvnw test -Dtest=AuthTokenServiceTest`

Expected: 编译失败。

- [ ] **Step 3: 实现认证**

实现 BCrypt 密码登录、Redis 哈希会话、安全 Cookie、登出、当前用户接口和 SAML 可配置入口。`SecurityConfig` 放行登录、SAML 元数据、健康检查和静态资源，其余 API 需认证；写接口启用 Cookie CSRF Token 校验，`WebCorsConfig` 仅允许配置来源、凭据和 `GET`、`POST` 方法。跨域部署时 Cookie 使用 `SameSite=None; Secure`，同域部署使用 `SameSite=Lax`。

- [ ] **Step 4: 验证通过**

Run: `cd backend && ./mvnw test -Dtest=AuthTokenServiceTest,RedisTokenSessionStoreTest,WebCorsConfigTest,AuthServiceTest`

Expected: PASS。

---

### Task 4: 系统管理领域服务

**Files:**

- Create: `backend/src/main/java/com/edm/system/entity/User.java`
- Create: `backend/src/main/java/com/edm/system/entity/Group.java`
- Create: `backend/src/main/java/com/edm/system/entity/Permission.java`
- Create: `backend/src/main/java/com/edm/system/mapper/UserMapper.java`
- Create: `backend/src/main/java/com/edm/system/mapper/GroupMapper.java`
- Create: `backend/src/main/java/com/edm/system/mapper/PermissionMapper.java`
- Create: `backend/src/main/java/com/edm/system/UserService.java`
- Create: `backend/src/main/java/com/edm/system/GroupService.java`
- Create: `backend/src/main/java/com/edm/system/PermissionService.java`
- Test: `backend/src/test/java/com/edm/system/UserServiceTest.java`
- Test: `backend/src/test/java/com/edm/system/GroupServiceTest.java`

**Interfaces:**

- Produces: `UserService.page(UserQuery query): IPage<UserResponse>`。
- Produces: `UserService.create(UserCreateRequest request): UserResponse`。
- Produces: `UserService.update(Long id, UserUpdateRequest request): UserResponse`。
- Produces: `GroupService.assignPermissions(Long groupId, Set<Long> permissionIds): GroupResponse`。

- [ ] **Step 1: 写失败测试**

测试必须覆盖：

- 创建用户时密码被 `PasswordEncoder` 哈希。
- 禁用用户不能登录。
- 重复用户名和邮箱返回业务错误。
- 群组权限可分配且返回权限编码。
- 无 `user:write` 权限调用创建方法被拒绝。

- [ ] **Step 2: 验证失败**

Run: `cd backend && ./mvnw test -Dtest=UserServiceTest,GroupServiceTest`

Expected: 编译失败。

- [ ] **Step 3: 实现实体、仓储与服务**

实体使用 `@TableName` 映射大写表名，Mapper 继承 `BaseMapper<T>`。服务层通过 MyBatis-Plus `Page`、`QueryWrapper` 和自定义 SQL 执行唯一性校验、权限校验、状态变更和审计日志。API 返回不包含 `PASSWORD_HASH`。

- [ ] **Step 4: 验证通过**

Run: `cd backend && ./mvnw test -Dtest=UserServiceTest,GroupServiceTest`

Expected: PASS。

---

### Task 5: 供应商与任务服务

**Files:**

- Create: `backend/src/main/java/com/edm/supplier/Supplier.java`
- Create: `backend/src/main/java/com/edm/supplier/SupplierMapper.java`
- Create: `backend/src/main/java/com/edm/supplier/SupplierService.java`
- Create: `backend/src/main/java/com/edm/task/DataTask.java`
- Create: `backend/src/main/java/com/edm/task/DataTaskMapper.java`
- Create: `backend/src/main/java/com/edm/task/DataTaskService.java`
- Test: `backend/src/test/java/com/edm/supplier/SupplierServiceTest.java`
- Test: `backend/src/test/java/com/edm/task/DataTaskServiceTest.java`

**Interfaces:**

- Produces: `SupplierService.page(SupplierQuery query): IPage<SupplierResponse>`。
- Produces: `SupplierService.create(SupplierCreateRequest request): SupplierResponse`。
- Produces: `SupplierService.update(Long id, SupplierUpdateRequest request): SupplierResponse`。
- Produces: `DataTaskService.page(DataTaskQuery query): IPage<DataTaskResponse>`。
- Produces: `DataTaskService.detail(Long id): DataTaskDetailResponse`。
- Produces: `DataTaskService.retry(Long id): DataTaskResponse`。

- [ ] **Step 1: 写失败测试**

测试必须覆盖：

- 供应商编码唯一。
- `FETCHER_TYPE` 只允许 `SFTP`、`REST`。
- `UPDATE_FREQUENCY >= 1`。
- 无 `supplier:write` 权限不能修改。
- 任务分页筛选。
- 三种失败状态分别累计对应重试次数并重置为 `PENDING`。
- 非失败状态重试被拒绝。
- 重试写审计日志。

- [ ] **Step 2: 验证失败**

Run: `cd backend && ./mvnw test -Dtest=SupplierServiceTest,DataTaskServiceTest`

Expected: 编译失败。

- [ ] **Step 3: 实现供应商与任务服务**

控制器保持轻薄，校验和业务规则在 service 层完成。任务查询使用 MyBatis-Plus `QueryWrapper` 和 `Page`，分页排序固定使用 `CREATE_TIME DESC, ID DESC`。

- [ ] **Step 4: 验证通过**

Run: `cd backend && ./mvnw test -Dtest=SupplierServiceTest,DataTaskServiceTest`

Expected: PASS。

---

### Task 6: REST 控制器与 Dashboard

**Files:**

- Create: `backend/src/main/java/com/edm/config/OpenApiConfig.java`
- Create: `backend/src/main/java/com/edm/supplier/SupplierController.java`
- Create: `backend/src/main/java/com/edm/task/DataTaskController.java`
- Create: `backend/src/main/java/com/edm/dashboard/DashboardController.java`
- Create: `backend/src/main/java/com/edm/dashboard/DashboardService.java`
- Create: `backend/src/main/java/com/edm/system/UserController.java`
- Create: `backend/src/main/java/com/edm/system/GroupController.java`
- Create: `backend/src/main/java/com/edm/system/PermissionController.java`
- Test: `backend/src/test/java/com/edm/dashboard/DashboardServiceTest.java`
- Test: `backend/src/test/java/com/edm/api/ApiSmokeTest.java`
- Test: `backend/src/test/java/com/edm/config/OpenApiConfigTest.java`
- Test: `backend/src/test/java/com/edm/api/SwaggerAccessTest.java`

**Interfaces:**

- Produces: `/api/auth/**`、`/api/suppliers/**`、`/api/tasks/**`、`/api/dashboard/today`、`/api/users/**`、`/api/groups/**`、`/api/permissions`。
- Produces: 查询接口全部为 `GET`，创建、更新、启用、禁用、重试和权限分配接口全部为 `POST`。
- Produces: OpenAPI 3 文档 `/v3/api-docs`、分组 `/v3/api-docs/management` 和 Swagger UI `/swagger-ui.html`。
- Produces: `CookieAuth`、`CsrfHeader` 两个 OpenAPI 3 安全方案。

端点映射固定如下：

| 模块 | GET | POST |
|---|---|---|
| 认证 | `/api/auth/saml/enabled`、`/api/auth/me` | `/api/auth/login`、`/api/auth/logout` |
| 供应商 | `/api/suppliers` | `/api/suppliers`、`/api/suppliers/{id}/update`、`/api/suppliers/{id}/enable`、`/api/suppliers/{id}/disable` |
| 任务 | `/api/tasks`、`/api/tasks/{id}` | `/api/tasks/{id}/retry` |
| Dashboard | `/api/dashboard/today` | 无 |
| 用户 | `/api/users` | `/api/users`、`/api/users/{id}/update`、`/api/users/{id}/enable`、`/api/users/{id}/disable` |
| 群组 | `/api/groups` | `/api/groups`、`/api/groups/{id}/update`、`/api/groups/{id}/permissions` |
| 权限 | `/api/permissions` | 无 |

- [ ] **Step 1: 写 Dashboard 失败测试**

构造多条不同状态、不同供应商、不同创建日期的任务，断言：

- 只统计当日任务。
- 状态数量正确。
- 供应商分布正确。
- 成功率使用 `COMPLETED / TOTAL`。
- 非当日数据不进入统计。

`ApiSmokeTest` 同时断言：

- 所有控制器只出现 `@GetMapping` 和 `@PostMapping`。
- 不存在 `@PutMapping`、`@PatchMapping`、`@DeleteMapping`。
- 变更接口全部为 `POST`。

`OpenApiConfigTest` 和 `SwaggerAccessTest` 必须断言：

- OpenAPI 版本为 3。
- 文档标题为“外部数据管理平台 Web 管理端 API”。
- 管理端分组名称为 `management`。
- `CookieAuth` 是 Cookie 中的 `EDM_TOKEN`。
- `CsrfHeader` 是请求头中的 `X-XSRF-TOKEN`。
- 登录接口不要求安全方案，业务接口同时声明两个安全方案。
- OpenAPI 操作只包含 `get` 和 `post`。
- `EDM_SWAGGER_ENABLED=false` 时文档和 Swagger UI 均不可访问。
- `EDM_SWAGGER_ENABLED=true` 时文档和 Swagger UI 可访问、复用 CORS 白名单，并携带 Cookie 凭证。

- [ ] **Step 2: 验证失败**

Run: `cd backend && ./mvnw test -Dtest=DashboardServiceTest`

Expected: 编译失败。

- [ ] **Step 3: 实现控制器和聚合服务**

所有控制器只做参数绑定和调用 service，返回 `ApiResponse`。控制器和 DTO 使用 `@Tag`、`@Operation`、`@Parameter`、`@SecurityRequirement`、`@Schema` 提供中文 OpenAPI 3 说明。Dashboard 统计在 service 层完成。`application.yml` 按规格配置 springdoc 文档路径、管理端分组、Swagger UI 路径、`with-credentials` 和 CSRF 支持。

- [ ] **Step 4: 验证通过**

Run: `cd backend && ./mvnw test -Dtest=DashboardServiceTest,ApiSmokeTest,OpenApiConfigTest,SwaggerAccessTest`

Expected: PASS。

---

### Task 7: 前端工程与设计系统

**Files:**

- Create: `frontend/package.json`
- Create: `frontend/vitest.config.ts`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tailwind.config.js`
- Create: `frontend/postcss.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/styles/theme.css`
- Create: `frontend/src/api/http.ts`
- Create: `frontend/src/api/types.ts`
- Create: `frontend/src/locales/zh-CN.ts`
- Create: `frontend/src/locales/zh-TW.ts`
- Create: `frontend/src/locales/en.ts`
- Test: `frontend/src/api/__tests__/http.spec.ts`

**Interfaces:**

- Produces: `http.get<T>(url, config)`、`http.post<T>(url, data)`、`http.put<T>(url, data)`。
- Produces: `ApiResponse<T>`、`PageResponse<T>`、`DataTask`、`Supplier`、`User`、`Group`、`Permission` TypeScript 类型。
- Produces: `npm run test`，使用 Vitest `^1.6.x` 和 jsdom `^20.x`。

- [ ] **Step 1: 写 HTTP 失败测试**

测试必须覆盖：

- 自动携带 Cookie。
- 开发环境默认同域代理。
- 生产跨域请求使用显式来源和 `withCredentials`。
- 写请求自动携带 `X-XSRF-TOKEN`。
- 响应 `code !== 0` 时抛出业务错误。
- HTTP 错误转换为用户可读错误。
- 不做任何 mock fallback。

- [ ] **Step 2: 验证失败**

Run: `cd frontend && npm run test`

Expected: `package.json` 不存在，命令失败。

- [ ] **Step 3: 初始化前端工程**

版本严格遵守全局约束，测试依赖使用 Vitest `^1.6.x` 和 jsdom `^20.x`。`theme.css` 完整映射设计规范 Token；Tailwind 扩展颜色、圆角和字号；axios 实例设置 `withCredentials: true`，并按 `XSRF-TOKEN` Cookie 自动发送 `X-XSRF-TOKEN` 请求头。开发环境通过 Vite proxy 访问后端，生产环境通过 API base URL 与后端 CORS 白名单对接。

- [ ] **Step 4: 验证构建**

Run: `cd frontend && npm run build`

Expected: PASS，且不产生提交的 `dist`。

---

### Task 8: 登录、布局、路由与状态

**Files:**

- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/stores/locale.ts`
- Create: `frontend/src/layouts/AdminLayout.vue`
- Create: `frontend/src/pages/LoginPage.vue`
- Create: `frontend/src/components/AppSidebar.vue`
- Create: `frontend/src/components/AppTopbar.vue`

**Interfaces:**

- Produces: 路由 `/login`、`/dashboard`、`/tasks`、`/suppliers`、`/system/users`、`/system/groups`、`/system/permissions`。
- Produces: `useAuthStore()`，包含 `login`、`logout`、`fetchMe`、`hasPermission`。
- Produces: `useLocaleStore()`，包含 `locale`、`setLocale`。

- [ ] **Step 1: 实现登录页与全局状态**

登录页按设计规范实现，支持账号密码和按后端状态显示的 SSO 按钮。登录失败使用 Element Plus 明确提示，不降级为本地数据。

- [ ] **Step 2: 实现管理布局**

左侧导航、顶部栏、语言切换、用户菜单和退出入口。导航按权限隐藏无权菜单，但安全仍由后端保障。

- [ ] **Step 3: 实现路由守卫**

未登录访问管理页跳转 `/login`；已登录访问 `/login` 跳转 `/dashboard`。

- [ ] **Step 4: 验证构建**

Run: `cd frontend && npm run build`

Expected: PASS。

---

### Task 9: 管理页面

**Files:**

- Create: `frontend/src/pages/DashboardPage.vue`
- Create: `frontend/src/pages/DataTaskPage.vue`
- Create: `frontend/src/pages/SupplierPage.vue`
- Create: `frontend/src/pages/system/UserPage.vue`
- Create: `frontend/src/pages/system/GroupPage.vue`
- Create: `frontend/src/pages/system/PermissionPage.vue`
- Create: `frontend/src/components/StatusBadge.vue`
- Create: `frontend/src/components/PageHeader.vue`

**Interfaces:**

- Consumes: Task 7 的 API 客户端和类型。
- Consumes: Task 8 的认证、权限和语言状态。

- [ ] **Step 1: 实现 Dashboard**

展示当日总数、成功、失败、待处理、处理中和供应商分布，所有数据来自 `/api/dashboard/today`。

- [ ] **Step 2: 实现任务页**

提供筛选、分页、详情抽屉和失败任务重试按钮。重试前必须确认，接口失败显示后端错误。

- [ ] **Step 3: 实现供应商页**

提供查询、创建、编辑、启用、禁用。表单校验与后端规则一致，禁用操作需要确认。

- [ ] **Step 4: 实现系统管理页**

用户页管理用户和群组关系；群组页管理群组和权限；权限页只读展示权限。所有写操作均走真实 API。

- [ ] **Step 5: 验证构建**

Run: `cd frontend && npm run build`

Expected: PASS。

---

### Task 10: 集成验证与交付文档

**Files:**

- Create: `README.md`
- Create: `backend/src/main/resources/db/migration/V2__admin_bootstrap.sql`
- Modify: `backend/src/main/resources/application.yml`

**Interfaces:**

- Produces: 中文启动与配置文档。
- Produces: 管理员引导迁移，不硬编码默认弱密码。

- [ ] **Step 1: 写管理员引导策略**

`V2__admin_bootstrap.sql` 只创建权限、群组和禁用的管理员占位记录；应用启动时从 `EDM_ADMIN_USERNAME` 和 `EDM_ADMIN_PASSWORD` 环境变量更新该管理员。环境变量缺失时不启用密码登录，并输出安全告警。

- [ ] **Step 2: 后端全量验证**

Run: `cd backend && ./mvnw test`

Expected: PASS。

- [ ] **Step 3: 前端全量验证**

Run: `cd frontend && npm run build`

Expected: PASS。

- [ ] **Step 4: 启动检查**

使用 `docker compose up -d mysql redis` 启动依赖，启动后端和前端开发服务，检查登录、Dashboard、任务、供应商和系统管理页面能访问真实 API。清理测试产生的数据。

- [ ] **Step 5: 交付检查**

确认：

- `git status` 中无 `frontend/dist`。
- 所有新增文档为中文。
- API 无敏感信息泄露。
- 未执行未经要求的 Git commit。
