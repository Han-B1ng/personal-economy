# 项目结构

代码已按用户指定的标准分层迁移，开发约定见根目录 [AGENTS.md](../AGENTS.md)。

```text
frontend/src/
├── api/                 公共HTTP、首页及工作区请求
├── assets/styles/       全局样式、业务样式与视觉规范
├── components/common/   公共按钮、输入、状态、对话框、页标题
├── hooks/               组合式工作区加载
├── layout/              移动布局、主导航
├── router/              路由及导航配置
├── store/               首页和工作区Pinia状态
├── types/               类型定义
├── utils/               日期/金额格式和图表计算
├── views/               home/growth/analytics/finance/profile/system
├── App.vue
└── main.ts
```

页面专用组件放在 `views/<业务>/components`，公共组件放在 `components/common`。页面通过store与api层读写数据，不直接拼HTTP请求。使用 `@/` 指向src。现有测试保留在 `frontend/tests/unit`。

```text
backend/src/main/java/com/personaleconomy/
├── controller/          HTTP入口
├── service/             查询及工作区服务接口
│   └── impl/            共用业务规则、MySQL/文件存储及演示行情
├── mapper/              参数化数据库读写接口
├── entity/              数据库映射对象
├── dto/                 校验请求输入
├── vo/                  返回对象
├── config/              本机请求访问约束
├── common/              公共响应、异常和健康检查
└── PersonalEconomyApplication.java
```

资源：application配置区分dev/prod；fixtures是演示种子。MyBatis XML在resources/mybatis，Flyway迁移在resources/db/migration；本地MySQL接入说明见[MYSQL_LOCAL.md](MYSQL_LOCAL.md)。

| 修改内容 | 入口 |
|---|---|
| 全局颜色、字号、间距 | assets/styles/tokens.css |
| 通用组件 | components/common |
| 页面布局与导航 | layout、router |
| 各模块页面 | views/<业务> |
| 前端接口和返回转换 | api |
| 页面状态与请求取消 | store、hooks |
| 金额/日期/图表 | utils |
| 后端接口 | controller |
| 请求校验 | dto |
| 返回字段 | vo |
| 服务实现及存储替换 | service/impl |

不通过目录分层宣称分布式或生产安全已完成。本机单用户存储、MySQL多用户持久化及认证分别验收；新事件待评估与固定演示行情分开。
