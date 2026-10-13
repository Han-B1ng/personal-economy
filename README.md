# Personal Economy

**Personal Economy — If life had a stock chart.**

**如果人生也有一张走势图。**

A personal growth tracking platform that transforms goals, tasks, and achievements into a simulated stock index, visualizing life's progress through financial-inspired analytics.

一款将个人目标、待办与成长成果转化为模拟股票指数的个人成长管理平台，通过金融行情可视化记录、分析与回顾成长历程。

移动端个人成长行情应用。本仓库提供工程代码与启动说明；设计及项目跟踪资料不包含在公开仓库中。

当前工程提供前后端启动骨架和连通检查页。目标、待办、事件、行情、财务等产品页面及业务接口尚未实现；不创建数据库表，不计算成长指数。

## 环境

- Node.js 22.12+（`.nvmrc` 固定本项目使用的 22.22.3），npm。
- JDK 17；Maven Wrapper 固定 Maven 3.9.16，首次运行需要网络。
- 可选：Docker Compose 和 MySQL 8.4 LTS。

后端使用 Spring Boot 3.5.16、MyBatis Starter 3.0.5。前端使用 Vue 3、TypeScript、Vite、Vue Router 和 Pinia；依赖版本由 `frontend/package-lock.json` 锁定。

## 本地启动

在项目根目录打开终端：

```bash
./scripts/backend.sh spring-boot:run
```

macOS 脚本自动选择 JDK 17；其他系统需预先配置 `JAVA_HOME`。默认 `bootstrap` profile 不连接数据库，后端监听 `http://127.0.0.1:8080`。

另开终端：

```bash
cd frontend
npm ci
npm run dev
```

打开 `http://127.0.0.1:5173`。页面显示工程连接状态，`/api` 经 Vite 代理至后端。端口已占用时启动会明确失败；后端地址可在 `frontend/.env.local` 设置 `VITE_API_PROXY_TARGET`。

可验证的接口：

- `GET /api/v1/health`：返回 `{ "code": "OK", "message": "成功", "data": { "status": "UP", "service": "personal-economy-backend" } }`。
- `GET /actuator/health`：进程健康检查。

以上检查只验证当前服务可响应，不代表数据库、用户认证或业务功能已就绪。工程目前仅供本地开发，后端默认绑定回环地址。

## 检查与构建

```bash
./scripts/backend.sh verify
cd frontend
npm run check
npm run format:check
```

后端产物位于 `backend/target/`；前端产物位于 `frontend/dist/`。前端 `check` 包含 ESLint、类型检查、API 边界测试和生产构建。后端集成测试覆盖无数据库启动、健康响应、404 和 405 状态。下载缓存保存在根目录 `.cache/`，不提交版本控制。

## 可选数据库

复制根目录 `.env.example` 为 `.env`，填写本地数据库密码，然后运行：

```bash
docker compose --profile database up -d mysql
```

MySQL 仅绑定 `127.0.0.1`，使用命名卷持久化数据。`mysql:8.4` 跟随该 LTS 系列补丁；正式部署前应另行固定镜像摘要。初始化只准备数据库实例，不添加业务表或示例数据。

本机目前提供独立的 `docker-compose` 命令；若 `docker compose` 不可用，将上面的命令替换为 `docker-compose --profile database up -d mysql`，停止命令同理。本轮仅校验 Compose 配置，未启动数据库容器。

随后在终端或 IDE 设置 `backend/.env.example` 中的变量，使用 `SPRING_PROFILES_ACTIVE=mysql` 启动后端。Spring Boot 不自动读取 `.env`，Compose 的密码也不会自动传入后端。Flyway 已添加依赖但暂时关闭，须在数据设计确认后编写并审核迁移。

停止数据库（保留数据卷）：

```bash
docker compose --profile database stop mysql
```

## 目录与约束

- `frontend/src/router`：页面路由；`views`：页面；`shared/api`：接口响应和请求封装。
- `backend/src/main/java/com/personaleconomy/common`：公共响应、异常处理与健康接口。
- `backend/src/main/resources`：环境配置，数据库 profile 与默认无数据库 profile 分离。
- 后续业务按模块增加 controller/service/mapper/DTO；用户数据隔离和认证仍待实现。

普通待办完成不能直接推动指数上涨。成长事件权重、动能衰减、目标完成规则、资产快照与收支口径应依照确认后的需求实现；工程初始化未补充这些规则。

技术版本依据：[Spring Boot 3.5 环境要求](https://docs.spring.io/spring-boot/3.5/system-requirements.html)、[MyBatis Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)、[Vite 环境要求](https://vite.dev/guide/)。
