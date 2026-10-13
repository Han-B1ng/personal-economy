# Personal Economy Frontend

Vue 3 + TypeScript + Vite，采用用户指定的标准分层，页面位于 `src/views`，接口在 `src/api`，状态在 `src/store`。

启动：`npm ci`、`npm run dev`。默认需要运行dev/mysql本机后端，启动见 [MySQL说明](../docs/MYSQL_LOCAL.md)。首页行情是演示数据，其余目标/待办/事件/财务/资料使用本地后端读写接口。

本轮只执行类型、静态与构建检查，功能及视觉验收交由用户完成。详见 [本地验收说明](../docs/LOCAL_ACCEPTANCE.md)、[项目结构](../docs/PROJECT_STRUCTURE.md)。

字体Inter Tight与Noto Sans SC通过npm包本地打包；OFL许可证位于public/licenses。不要在VITE环境变量中存放私密凭据。
