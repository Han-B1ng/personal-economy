# Personal Economy

**Personal Economy — If life had a stock chart.**

**如果人生也有一张走势图。**

A personal growth tracking platform that transforms goals, tasks, and achievements into a simulated stock index, visualizing life's progress through financial-inspired analytics.

一款将个人目标、待办与成长成果转化为模拟股票指数的个人成长管理平台，通过金融行情可视化记录、分析与回顾成长历程。

移动端个人成长行情应用。本仓库提供工程代码与启动说明；设计及项目跟踪资料不包含在公开仓库中。

当前已实现五模块页面及目标、待办、事件、财务与资料的核心操作，接入本机单用户工作区接口。业务数据已接入本机MySQL，保留旧文件模式作为显式备用。行情仍是明确标记的固定演示数据；登录认证和指数评估未实现。请阅读 [本地验收说明](docs/LOCAL_ACCEPTANCE.md) 后进行功能及视觉验收。

## 环境

- Node.js 22.12+（`.nvmrc` 固定本项目使用的 22.22.3），npm。
- JDK 17；Maven Wrapper 固定 Maven 3.9.16，首次运行需要网络。
- MySQL模式：Docker/Colima、Compose、MySQL 8.4 LTS及Python 3启动脚本。

后端使用 Spring Boot 3.5.16、MyBatis Starter 3.0.5。前端使用 Vue 3、TypeScript、Vite、Vue Router 和 Pinia；依赖版本由 `frontend/package-lock.json` 锁定。

## 本地启动

在项目根目录打开终端：

```bash
docker-compose --project-name personal-economy --profile database up -d mysql
python3 scripts/backend-mysql.py
```

首次使用先配置根 `.env`，步骤见 [MySQL本地说明](docs/MYSQL_LOCAL.md)；本机已有配置不要覆盖。macOS脚本自动选择JDK17；默认 `dev` profile 连接MySQL，后端监听 `http://127.0.0.1:8080`。

另开终端：

```bash
cd frontend
npm ci
npm run dev
```

打开 `http://127.0.0.1:5173` 查看首页，默认需要启动MySQL后端。仅想离线审查页面时，可在 `frontend/.env.local` 设置 `VITE_HOME_DATA_SOURCE=demo` 并重启前端；默认值为 `api`，连接失败不会自动回退。工程连通检查页位于 `/dev/connection`；`/api` 经 Vite 代理至后端。端口已占用时启动会明确失败；后端地址可在 `frontend/.env.local` 设置 `VITE_API_PROXY_TARGET`。

开发模式下可访问 `/?preview=empty` 与 `/?preview=error` 检查首页空状态和失败重试；这些查询参数不作用于生产构建。周期筛选使用演示数据的最新记录时间，较长周期不会补造早期历史。走势图支持触控拖动和左右方向键查看节点，Home/End 跳到首尾，Escape 取消选择。

可验证的接口：

- `GET /api/v1/home`：固定演示行情及工作区最新事件，包含 `source: demo`，在本机 `dev/mysql` 或显式 `bootstrap` 中提供，`prod` 禁用。字段及边界见 [首页查询契约](docs/api/home.md)。
- `GET /api/v1/health`：返回 `{ "code": "OK", "message": "成功", "data": { "status": "UP", "service": "personal-economy-backend" } }`。
- `GET /actuator/health`：进程健康检查。

健康检查和接口响应不能代替完整业务验收；当前MySQL持久化已接入，用户认证仍未实现。工程目前仅供本地开发，后端默认绑定回环地址。

本轮首页与框架优化及局部阶段检查见 [阶段说明](docs/FRONTEND_HOME_POLISH.md)。

## 检查与构建

本轮按新增阶段测试要求运行首页/公共组件回归和只读布局检查；完整业务与视觉验收由用户统一进行。构建通过不等于产品验收通过，范围与结果见阶段说明。

```bash
./scripts/backend.sh verify
cd frontend
npm run check
npm run format:check
```

后端产物位于 `backend/target/`；前端产物位于 `frontend/dist/`。前端 `check` 包含 ESLint、类型检查、API 边界测试和生产构建。后端集成测试覆盖无数据库启动、健康响应、404 和 405 状态。下载缓存保存在根目录 `.cache/`，不提交版本控制。

## 数据库

[MySQL本地开发说明](docs/MYSQL_LOCAL.md)包含环境启动、表与接口映射、首次JSON导入、版本化迁移、备份和停止方法。业务schema为本地V1开发实现，正式评审及身份/指数模型仍待完成。

启动会先运行Flyway，再只导入一次现有 `data/workspace.json`（缺少文件才使用种子）。导入成功后MySQL为主数据源，后续重启不覆盖。API返回 `source: local-mysql`；首页行情仍标为 `demo`。

旧文件模式需显式启动：`SPRING_PROFILES_ACTIVE=bootstrap ./scripts/backend.sh spring-boot:run`。它不包含MySQL模式的新变更，不可当作数据库备份。

## 目录与约束

- 前端使用 `api/assets/components/hooks/layout/router/store/types/utils/views` 标准分层。
- 后端使用 `controller/service/service/impl/dto/vo/config`，返回对象与请求对象分开。
- 数据库实体在 `entity`，Mapper接口在 `mapper`，MyBatis XML在 `resources/mybatis`，Flyway迁移在 `resources/db/migration`。
- 目录职责与修改入口见 [项目结构说明](docs/PROJECT_STRUCTURE.md)。
- MySQL使用命名卷持久化；原工作区与备份在 `data`，本地密码在 `.env`，均不公开上传。

普通待办完成不能直接推动指数上涨。成长事件权重、动能衰减、目标完成规则、资产快照与收支口径应依照确认后的需求实现；工程初始化未补充这些规则。

技术版本依据：[Spring Boot 3.5 环境要求](https://docs.spring.io/spring-boot/3.5/system-requirements.html)、[MyBatis Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)、[Vite 环境要求](https://vite.dev/guide/)。
