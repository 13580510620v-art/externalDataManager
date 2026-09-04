# 外部数据管理系统

外部数据管理系统用于统一管理外部数据供应商、数据文件任务、系统用户、群组和权限。当前仓库实现 Web 管理端，后端对接 MySQL 8、Redis 8 和 S3 兼容对象存储的任务数据模型，前端提供多语言管理界面。

## 功能范围

- 用户名密码登录与可配置 SAML2 SSO 登录。
- 统一 Opaque Token 会话，Token 明文仅存于 HttpOnly Cookie，Redis 中仅保存 SHA-256 哈希。
- Dashboard 当日任务总量、完成量、成功率、失败量、待处理量、处理中和供应商分布。
- 数据任务查询、筛选、详情和失败任务重试。
- 供应商配置的新建、编辑、启用和禁用。
- 用户、群组、权限管理，后端统一执行 RBAC 授权。
- 中文简体、中文繁体、英文界面。
- OpenAPI 3 / Swagger 3 文档，默认关闭，可通过环境变量开启。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3.5.6、Spring Security、MyBatis-Plus 3.5.12 |
| 数据库 | MySQL 8、Flyway、Redis 8、Redisson |
| 前端 | Vue 3.5、Vite 4.5、TypeScript 5.6、Element Plus 2.9、Tailwind CSS 3.4、Pinia 2.2 |
| 文档 | springdoc-openapi 2.8.17 / OpenAPI 3 |

## 环境要求

- JDK 17
- Node.js 18.12.1
- Docker Desktop 或兼容 Docker Compose 命令
- 可用端口：`3306`、`6379`、`8080`、`5173`

Node 版本检查：

```bash
node -v
```

如果本机使用 fnm，可执行：

```bash
fnm install 18.12.1
eval "$(fnm env)"
fnm use 18.12.1
```

## 启动依赖

```bash
docker compose up -d mysql redis
```

## 启动后端

首次启动需要配置管理员引导账号。管理员密码至少 8 位，不写入代码或迁移：

```bash
export MYSQL_USERNAME=edm
export MYSQL_PASSWORD=edm_password
export EDM_ADMIN_USERNAME=admin
export EDM_ADMIN_PASSWORD='Admin@123456'
export EDM_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173

cd backend
./mvnw spring-boot:run
```

后端默认地址：`http://localhost:8080`。

未配置 `EDM_ADMIN_USERNAME` 或 `EDM_ADMIN_PASSWORD` 时，管理员密码登录保持禁用，并输出安全告警；已配置时，启动过程会启用管理员、使用 BCrypt 哈希密码并加入 `ADMIN` 群组。

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`。开发环境通过 Vite 同域代理访问 `/api`，避免浏览器跨域；生产部署时设置 `VITE_API_BASE_URL`，并把它加入后端 `EDM_CORS_ALLOWED_ORIGINS` 白名单。

## Swagger 3 / OpenAPI 3

默认关闭。需要开启时：

```bash
export EDM_SWAGGER_ENABLED=true
```

访问地址：

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- 管理端分组：`http://localhost:8080/v3/api-docs/management`

接口文档包含 `CookieAuth` 与 `CsrfHeader` 安全方案，分别对应 Cookie `EDM_TOKEN` 和请求头 `X-XSRF-TOKEN`。

## SAML2 SSO 配置

SAML 默认关闭。开启前需预先创建带 SAML Name ID 的用户：

```bash
export EDM_SAML_ENABLED=true
export EDM_SAML_IDP_METADATA_URL='https://idp.example.com/metadata'
export EDM_SAML_ENTITY_ID='external-data-manager'
export EDM_SAML_BASE_URL='https://edm.example.com'
export EDM_SAML_SUCCESS_URL='https://console.example.com/dashboard'
export EDM_SAML_FAILURE_URL='https://console.example.com/login?samlError=1'
```

## 测试与构建

后端全量测试：

```bash
cd backend
./mvnw test
```

前端测试与构建：

```bash
cd frontend
npm run test
npm run build
```

## API 约定

- 统一响应结构：`code`、`message`、`data`。
- 自定义接口仅使用 `GET` 与 `POST`。
- 查询接口使用 `GET`，创建、更新、启用、禁用、重试、权限分配全部使用 `POST`。
- 跨域来源必须显式配置，禁止 `*`。
- 写请求启用 CSRF Cookie 与 `X-XSRF-TOKEN` 请求头。

## 目录结构

```text
backend/   Spring Boot 后端
frontend/  Vue 3 Web 管理端
docs/      需求规格与实施计划
```
