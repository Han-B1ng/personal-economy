# Personal Economy：S1 私有 Docker 测试部署

操作者逐步执行；本文件不会自动连接服务器。这是单个共享工作区的内测环境，入口密码不是产品账号体系。真实注册、用户隔离与正式生产模式尚未完成。所有接口经过入口 Basic Auth；浏览器通过 Tailscale HTTPS 访问。

## 1. 在 Mac 项目根目录打包

```bash
bash scripts/package-preview.sh
```

需要 Node >=22.12、JDK17、可运行的 Docker 和 Python3。脚本根据锁文件安装前端依赖，构建 Vue 和 Java，再构建三份 Linux AMD64 镜像，输出 `.cache/releases/<标签>/personal-economy-<标签>.tar.gz`。首次需要下载基础镜像，可能较慢。不执行测试套件、不连接服务器、不停止本地服务。发布包只含构建产物和部署配置，不包含设计稿、跟踪资料、源码环境文件或本机数据库。

## 2. 在 Mac 上传（将路径中的标签换为打包输出的实际值）

```bash
scp '.cache/releases/<标签>/personal-economy-<标签>.tar.gz' root@100.79.89.116:/tmp/
ssh root@100.79.89.116
```

连接由操作者执行。以下所有命令在 S1 执行。先检查当前状态：

```bash
docker version
docker compose version
free -h
df -h /
ss -lntp
tailscale serve status
```

确认 127.0.0.1:8088 空闲，Tailscale 的 HTTPS 8443 未被已有服务使用。保留现有 443 到 Hermes 的代理。

## 3. 首次安装到新目录

如果 `/opt/personal-economy` 已存在，停止此步骤并核对现状，不覆盖旧安装。下面的 mkdir 在目录已存在时会失败。

```bash
mkdir /opt/personal-economy
cd /opt/personal-economy
tar -xzf /tmp/personal-economy-<标签>.tar.gz
sha256sum -c SHA256SUMS
docker load -i images.tar.gz
cp .env.example .env
python3 init-preview.py
```

输入你自己的测试入口密码，默认用户名 `reviewer`。不要把密码或 secrets 文件发到聊天或提交 Git。脚本生成数据库密码与网关令牌，只运行一次；再次运行会拒绝覆盖。

核对 `.env` 的 `PE_PREVIEW_ORIGIN` 为你实际使用的完整 HTTPS Origin，包括 8443 端口。默认值是：

```text
https://vm-0-14-ubuntu.tail72af4b.ts.net:8443
```

## 4. 启动新项目

```bash
docker compose -f compose.preview.yaml config --quiet
docker compose -f compose.preview.yaml up -d --wait --wait-timeout 300
docker compose -f compose.preview.yaml ps
```

MySQL 和后端均不映射宿主端口；前端只监听 127.0.0.1:8088。资源上限分别是 MySQL 512MiB、后端 640MiB、前端96MiB，日志轮转10MiB×3。这些是上限，不是容量测试结果。服务器资源随 Hermes 负载变化，需要观察实际占用。

首次启动使用独立数据库卷和已有演示数据，本机数据库不会被迁移。迁移文件由 Flyway 自动执行。页面中的指数仍是演示行情，普通待办不会推动指数，新成长事件没有自动赋权。

检查未登录入口应返回 401：

```bash
curl -I http://127.0.0.1:8088/
```

## 5. 启用独立的 Tailscale HTTPS 入口

```bash
tailscale serve --bg --https=8443 http://127.0.0.1:8088
tailscale serve status
```

保持现有 443 配置，不执行 `tailscale serve reset`，不启用 Funnel，不开放公网安全组端口。浏览器设备需要加入同一 Tailnet、符合访问策略并使用上述 HTTPS URL。不要直接访问公网 IP 或明文 HTTP。

由用户验收：登录测试入口，读取首页，新增目标及待办，刷新验证持久化，完成普通待办并确认不自动增加指数。完整业务和视觉验收由用户进行。

## 排查与停止

```bash
docker compose -f compose.preview.yaml logs --tail=100 backend mysql frontend
docker stats --no-stream
```

发回日志前遮掉敏感信息。若启动失败，保留数据库及 secrets，停止应用：

```bash
docker compose -f compose.preview.yaml stop
```

如需撤掉本次 HTTPS 入口，仅关闭 8443：

```bash
tailscale serve --https=8443 off
```

不要执行 `down -v`、删除数据库卷、`docker system prune` 或 `tailscale serve reset`。Compose 项目名固定为 `personal-economy-preview`，不会接管 Hermes 服务。

## 后续更新与备份

更新前先做数据库逻辑备份并安全保管 secrets。本次不自动更新或导入本机数据。可执行（服务器项目目录）：

```bash
mkdir -p backups
chmod 700 backups
umask 077
docker compose -f compose.preview.yaml exec -T mysql sh -c 'MYSQL_PWD=$(cat /run/secrets/MYSQL_ROOT_PASSWORD) exec mysqldump -u root --single-transaction --no-tablespaces personal_economy' > "backups/personal-economy-$(date +%Y%m%d-%H%M%S).sql"
```

检查备份命令退出码及文件非空；恢复演练尚未执行。更新包只替换部署文件与镜像，保留 `.env`、`secrets` 和数据库卷，将 `.env` 的 RELEASE_TAG 改为新标签后再启动。旧镜像回滚不保证兼容未来数据库迁移，迁移变更前另行评审。
