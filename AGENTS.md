# Personal Economy 开发约定

用户于 2026-10-13 明确要求采用下面的标准分层结构。后续开发、重构和文档应遵循此约定，保持可读性，便于用户学习和长期维护。

## 前端目录

```text
frontend/src/
├── api/                 接口请求与响应转换
├── assets/              图片、字体和全局 CSS
├── components/
│   └── common/          全局通用组件
├── hooks/               Vue 组合式逻辑
├── layout/              全局布局及导航组件
├── router/              路由与导航配置
├── store/               Pinia 状态管理
├── types/               TypeScript 类型
├── utils/               工具函数
├── views/               业务页面，按 home/growth 等模块归类
├── App.vue
└── main.ts
```

- 使用明确命名，例如 `api/homeApi.ts`、`store/useHomeStore.ts`、`types/home.ts`，避免不明确的 `repository.ts`、`types.ts`。
- 页面专用组件放在所属 `views/<业务>/components`；跨页面复用的组件放在 `components/common`。
- 测试保持在 `frontend/tests`，按对应职责或业务分类；目录移动时同步修改测试导入。
- 保留 `@/` 导入别名，Vite、Vitest 和 TypeScript 配置一致。
- `public` 中的资源不经过模块转换，会原样复制到构建输出。需参与模块处理的资源放在 `src/assets`。
- `.env` 与 `.env.production` 不提交真实凭据；提交示例模板。`VITE_*` 会进入浏览器产物，不能存放秘密。

## 后端目录

Java 包根为 `com.personaleconomy`，采用：

```text
controller/             HTTP 请求接口
service/                业务接口
service/impl/           业务实现
mapper/                 MyBatis Mapper 接口
entity/                 数据库实体
dto/                    请求入参
vo/                     返回前端对象
config/                 配置
util/                   工具
PersonalEconomyApplication.java
```

- 返回前端的对象放在 `vo`；入参放在 `dto`；不能将数据库实体直接作为接口响应。
- Controller 不写 SQL，Mapper 不承担业务流程。实际需要数据库持久化时再创建 entity、mapper 和迁移。
- MyBatis XML 统一放在 `src/main/resources/mybatis`，同步调整扫描路径。
- 开发和生产配置分别采用 `application-dev.yml` / `application-prod.yml`。无数据库演示配置可单独保留，但不能冒充生产实现。
- 测试放在 `src/test/java`；包名移动后进行干净构建和接口回归。

## 现有代码与迁移

代码已迁移到上述标准分层目录，不再新增 `src/app`、`src/modules`、`src/shared` 或后端 `modules/home`。

用户明确要求独立开发后交由其测试与验收。后续按此分工：进行必要的类型、静态和构建检查，保留已有测试，但未经用户新指示不自动运行测试套件或浏览器验收；不能把构建通过写成测试通过。

不为凑齐目录创建空的业务类，也不在没有实际需要时增加分布式设施。设计与跟踪资料继续仅保留在本地，不加入公开代码提交。
