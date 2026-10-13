# MySQL 本地开发接入

当前接通的是 **Vue → Vite 代理 → Spring Boot → MyBatis → MySQL** 的本机单用户业务数据链路。目标、待办、事件、分类、资料、收支与资产快照真实落库；登录与指数评估另阶段实现。首页指数和43个历史点仍来自演示行情文件，不能把数据库接通理解为评分完成。

## 启动与连接

本机使用 Colima 和独立 `docker-compose` 命令；其他环境可以使用 Docker Desktop 和 `docker compose`。

```bash
# macOS 本机 Docker 尚未运行时
colima start --cpu 2 --memory 2

# 项目根目录；首次使用先复制 .env.example 为 .env，填写两个不同的强密码
# 本机已生成 .env，无需再次复制覆盖
# 始终使用同一个项目名，避免误接另一个数据卷
docker-compose --project-name personal-economy --profile database up -d mysql

# 读取根目录 .env 的数据库设置，选择 Java 17 和 dev profile
python3 scripts/backend-mysql.py

# 另开终端
cd frontend
npm run dev
```

- 前端：`http://127.0.0.1:5173`
- 后端：`http://127.0.0.1:8080`
- MySQL：`127.0.0.1:3306`，数据库与应用账号均为 `personal_economy`；以根 `.env` 的值为准。连接客户端的密码查看本地 `.env`，不要填入前端环境变量。
- MySQL只映射本机地址。数据卷：`personal-economy_mysql-data`。数据库不会随前后端进程退出而丢失。
- Spring Boot 不直接读取 `.env`；脚本只解析配置值，不执行文件中的Shell代码。外部提供的 `DB_URL/DB_USERNAME/DB_PASSWORD` 优先于 `.env`。
- 默认profile现为 `dev`，组内启用 `mysql`。`bootstrap` 是显式选择的旧文件模式，**两种模式不双写、不能混用**。
- 数据库连接或迁移失败时启动失败，不自动退回 JSON 模式。
- 当前 `prod` 启动被明确阻止：认证与正式数据库评审尚未完成，避免执行本地开发迁移。

## 当前表与接口映射

| 表 | 内容 | 现有接口 |
|---|---|---|
| `app_user` | 本机资料：昵称、简介、头像；不是注册账户 | `PUT /api/v1/profile` |
| `personal_stock` | 股票代码、初始1000点；未保存或计算实时行情 | 首页资料区域 |
| `workspace_state` | 首次导入标记、来源、工作区更新时间及事务锁 | `GET /api/v1/workspace` |
| `event_category` | 分类名称、系统/自定义标记、可用状态 | 分类新增/停用 |
| `growth_goal` | 标题、分类、描述、日期、手动进度、完成状态/日期 | 目标新增/编辑/完成 |
| `growth_task` | 标题、描述、日期/时间、优先级、可选目标、完成状态 | 待办新增/编辑/完成/删除 |
| `growth_event` | 标题、分类、描述、发生日期、参考重要程度、可选来源目标、评估状态/贡献 | 事件新增/编辑/删除 |
| `finance_record` | 类型、金额、输入分类、发生日期、备注 | 手动录入收支 |
| `asset_snapshot` | 每次独立保存的现金、其他资产、负债和时间；读取最新一条 | 手动保存资产快照 |
| `flyway_schema_history` | Flyway迁移执行与校验记录 | 无业务接口 |

实体在 `entity`，SQL在 `resources/mybatis/WorkspaceMapper.xml`，接口在 `mapper/WorkspaceMapper.java`；Controller只处理HTTP、DTO校验和VO响应。`AbstractWorkspaceService` 共用业务校验，`MysqlWorkspaceService` 管理事务、读取及差异落库，文件适配器保留作显式备用。

每条业务查询均绑定配置中的固定本机用户 `local-review-user`。业务主键/外键包含用户范围，SQL使用 `#{...}` 参数绑定。**这不等于登录认证或已验收多用户隔离**；请求不能传用户ID切换身份，本轮也未实现账户注册、密码存储或token。

写事务先锁定 `workspace_state` 行，再读取和校验，只写改变的业务行；多步失败整体回滚。读取工作区使用一个只读、可重复读事务，避免返回跨批次的组合数据。当前按工作区整包返回，后续数据量增大时再拆查询、分页与版本冲突处理。

金额使用 `DECIMAL(12,2)`；日期为 `DATE`；更新时间使用UTC `TIMESTAMP(6)`。分类停用保留历史关联，系统分类不可停用。目标不提供删除，待办和事件沿用现有删除接口；外键没有级联删除。每次资产保存追加快照，收支记录不会隐式修改它。

## 首次导入与迁移

启动顺序为：Flyway `V1__local_workspace.sql` → 首次工作区初始化 → 提供可用数据。

1. 数据库未初始化时读取 `PE_DATA_FILE`（脚本默认根 `data/workspace.json`）。文件存在但损坏会报错，不用种子替换。
2. 没有本地文件时才读取 `fixtures/workspace-demo.json`。
3. 资料、股票、分类、目标、待办、事件、财务与初始资产在一个事务中导入，保留原有业务ID、状态、日期和演示标记。失败回滚。
4. 导入成功后记录 `initialized=true`；后续重启直接读取MySQL，不重导、不覆盖。原JSON和导入前备份保留在忽略目录 `data` 中。
5. API快照 `source` 为 `local-mysql`；文件模式为 `local-demo`；首页行情单独保持 `source: demo`。

已执行的 V1 不再编辑。字段调整通过新增 V2、V3 等迁移推进；Flyway校验失败时排查迁移差异，不删除历史表或关闭校验绕过。此迁移是依据现有V1接口实施的**开发版本**，不能代替既有正式数据设计评审。

本机2026-10-13运行版本为MySQL8.4.11、Spring Boot3.5.16、MyBatis Starter3.0.5、Flyway11.7.2。Flyway日志提示MySQL8.4超过该版本声明的最高已验证版本8.1；当前V1迁移已实际成功执行。后续依赖升级和正式环境兼容性仍需验证，不隐藏此提示。相关版本支持说明见 [Flyway MySQL文档](https://documentation.red-gate.com/fd/mysql-277579322.html)。

## 备份与停止

先停止写操作，再导出备份。下面命令从容器环境读取应用账号密码，不在命令行填写密码：

```bash
umask 077
mkdir -p data/backups
docker exec personal-economy-mysql-1 sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysqldump --single-transaction --no-tablespaces -u"$MYSQL_USER" "$MYSQL_DATABASE"' > "data/backups/mysql-$(date +%Y%m%d-%H%M%S).sql"

# 停止但保留数据卷
docker-compose --project-name personal-economy --profile database stop mysql
```

不要用 `down -v` 作为日常停止命令，它会删除数据库卷。修改 `.env` 密码不会自动更新已初始化的数据卷账号；修改端口需同时重建容器并重启后端。

如需检查旧文件模式：停止MySQL后端，运行 `SPRING_PROFILES_ACTIVE=bootstrap ./scripts/backend.sh spring-boot:run`。旧文件只包含切换前的数据，MySQL后续变更不会同步回旧文件；切回旧文件不等于迁移回滚。

## 本轮检查与用户验收

本轮进行两端构建、前端源码静态检查、MySQL健康和迁移核对、前端代理读取、临时待办落库及后端重启回读；临时记录完成后删除。未运行测试套件或逐页浏览器验收。

请在 [本地验收清单](LOCAL_ACCEPTANCE.md) 基础上重点检查：保存后刷新仍存在、重启后仍存在、错误保存不伪成功、分类停用保留历史、资产和流水互不联动。待办完成不改目标进度或指数；新事件保持PENDING且贡献为空。

目标进度日志、正式指数历史/重算、评分权重、动能衰减、账户认证、正式schema评审、用户隔离验收仍待后续处理。

### 2026-10-13本机执行结果

- 已执行V1，9张业务表＋1张迁移历史表；首次导入6分类、3目标、4待办、5事件、5收支和1资产快照。
- 通过前端5173代理读取 `source: local-mysql`；临时待办写入后直接SQL查到，后端重启仍可读取，完成检查后已删除。
- 核对发现DECIMAL小数位归一化导致误判资产变更，已改为数值比较；清理本轮产生的两条完全相同的重复快照。针对修正再次核对临时待办创建/完成/删除，目标进度、资产和演示指数不变，资产表仍1条、待办恢复4条。
- 当前两端构建及前端源码ESLint通过；没有运行测试套件或逐页浏览器验收。
- 页面JSON导出仅包含当前工作区和最新资产快照，不包括历史资产快照或迁移记录；完整数据库备份使用上面的mysqldump方法。
