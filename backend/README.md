# 后端

Java17、Spring Boot3.5.16、MyBatis Starter3.0.5、MySQL8.4。本地启动与数据导入见 [MySQL说明](../docs/MYSQL_LOCAL.md)。

从根目录运行 `python3 scripts/backend-mysql.py`，加载根 `.env` 并以 `dev/mysql` 启动。正式生产模式目前被阻止，不能将本地固定用户接口当作认证实现。

代码采用 `controller → service → mapper`；DTO处理入参，VO处理返回，entity用于数据库映射，SQL在 `resources/mybatis`。Flyway迁移在 `resources/db/migration`。V1已执行后不能编辑，应新增后续版本。

`AbstractWorkspaceService` 共用当前业务校验；MySQL适配器在事务中锁定工作区、读取并写入差异。旧 `bootstrap` 文件适配器须显式启用，MySQL写入不会双写JSON。

当前支持目标、待办、事件、分类、收支、资产与资料读写。普通待办完成不影响指数；新事件仍待评估，行情文件仍为演示数据。完整测试交由用户，构建和必要连通检查不代表完成业务验收。
