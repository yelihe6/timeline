# 时间线 | Timeline

一个轻量级的时间与事件管理 Web 应用，支持事件列表、日历视图、自定义背景与主题。

![Vue 3](https://img.shields.io/badge/Vue-3.5-42b883?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6db33f?logo=springboot)
![License](https://img.shields.io/badge/license-MIT-blue)

## 功能概览

- **用户与鉴权**：注册、登录、验证码、找回密码（邮件验证码）
- **事件管理**：按日期添加/编辑/删除事件，支持批量删除
- **日历视图**：月历展示事件，与列表联动
- **自定义背景**：多图上传、轮播、位置与缩放调节、透明度
- **主题**：亮色 / 暗色切换
- **响应式**：适配不同屏幕

## 技术栈

| 端     | 技术 |
|--------|------|
| 前端   | Vue 3、Vue Router、Vite、Axios |
| 后端   | Spring Boot 3、MyBatis、MySQL 8、Spring Mail |
| 安全   | BCrypt 密码、Session、图形验证码 |

## 项目结构

```
time-project/
├── backend/                 # Spring Boot 3 后端 API
│   ├── src/main/java/       # Java 源码
│   └── src/main/resources/  # 配置与 SQL
├── frontend/                 # Vue 3 前端
│   ├── src/
│   │   ├── api/              # 接口封装
│   │   ├── components/      # 公共组件
│   │   ├── composables/     # 组合式逻辑
│   │   ├── views/           # 页面
│   │   └── router/          # 路由
│   └── index.html
├── .gitignore
└── README.md
```

## 环境要求

- **JDK 21**
- **Node.js** 18+
- **MySQL** 8.0+
- **Maven** 3.8+

## 快速开始

克隆仓库后，分别配置并启动后端与前端：

```bash
git clone https://github.com/<your-username>/time-project.git
cd time-project
```

### 1. 数据库

创建数据库并执行建表 SQL（见 `backend/src/main/resources/` 下或项目文档中的 SQL 脚本）。

```sql
CREATE DATABASE time CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 后端配置

敏感信息通过**环境变量**或**本地配置文件**提供，避免提交到仓库。

**方式一：环境变量（推荐）**

```bash
export DB_PASSWORD=你的数据库密码
export MAIL_USERNAME=你的邮箱
export MAIL_PASSWORD=邮箱授权码
```

**方式二：本地配置文件**

复制示例配置并填写实际值：

```bash
cd backend/src/main/resources
cp application-example.yml application-local.yml
# 编辑 application-local.yml，填写数据库与邮件信息
```

本地配置文件 `application-local.yml` 已在 `.gitignore` 中，不会提交。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认：<http://localhost:8080>

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认：<http://localhost:5173>，开发环境会将 `/api` 代理到后端。

### 5. 生产构建

```bash
# 前端
cd frontend && npm run build

# 后端
cd backend && mvn package
```

前端产物在 `frontend/dist`，可交由 Nginx 等托管；后端可运行 `java -jar backend/target/time-backend-1.0.0.jar`。

## 配置说明

### 数据库

| 变量（可选） | 说明     | 默认   |
|-------------|----------|--------|
| `DB_HOST`   | 数据库主机 | localhost |
| `DB_PORT`   | 端口     | 3306   |
| `DB_NAME`   | 库名     | time   |
| `DB_USERNAME` | 用户名 | root   |
| `DB_PASSWORD` | 密码   | 需设置 |

### 找回密码邮件（可选）

未配置或配置错误时，找回密码会提示「邮件服务未配置」。

| 变量（可选）   | 说明     |
|----------------|----------|
| `MAIL_HOST`    | SMTP 主机，如 smtp.qq.com |
| `MAIL_PORT`    | 端口，如 587 |
| `MAIL_USERNAME`| 发件邮箱 |
| `MAIL_PASSWORD`| 邮箱授权码（非登录密码） |

## 主要 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |
| GET  | /api/captcha | 图形验证码 |
| POST | /api/auth/forgot-password/send-code | 发送找回密码验证码 |
| POST | /api/auth/forgot-password/reset | 验证码重置密码 |
| GET  | /api/events | 分页查询事件 |
| POST | /api/events | 添加事件 |
| PUT  | /api/events/{id} | 更新事件 |
| DELETE | /api/events/{id} | 删除事件 |
| POST | /api/events/batch-delete | 批量删除事件 |

## 开源协议

[MIT](LICENSE)

## 贡献

欢迎提交 Issue 与 Pull Request。
