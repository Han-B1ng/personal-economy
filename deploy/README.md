# Personal Economy：S1 私有 Docker 测试部署

操作者逐步执行；本文件不会自动连接服务器。这是单个共享工作区的内测环境，入口密码不是产品账号体系。真实注册、用户隔离与正式生产模式尚未完成。所有接口经过入口 Basic Auth；浏览器通过 Tailscale HTTPS 访问。

## 推荐：从 GitHub 发布镜像

GitHub 的 Publish preview images 工作流在 main 上相关代码更新时运行，也可以在 Actions 页面手动 Run workflow。完成前端 lint/构建与 Java 打包后，发布三个 Linux AMD64 镜像到 GHCR。完整测试套件不自动运行；不会连接或启动 S1。所有 Actions 固定到提交版本。

每次运行使用源码提交、运行编号和重试次数组成独立发布标签。仅当三份镜像全部发布成功并完成 Compose 校验，才交付小型配置附件 `preview-deploy-<run-id>-<attempt>`；附件中的 Compose 固定镜像 digest，`release.json` 记录版本。数据库密码和网关令牌不进入附件或镜像。

### G1. 确认仓库可达与下载配置

在 S1 检查仓库接口（GET，不携带令牌；401 通常说明接口可达）：

```bash
curl --connect-timeout 10 --max-time 20 -sS -o /dev/null -w '%{http_code}\n' https://ghcr.io/v2/
```

接口响应不代表镜像层下载成功，后续 pull 才是实际验证。新 GHCR 包默认可能是私有；公开 GitHub 仓库不等于公开镜像。由用户确认包可见性，助手不自动修改。私有镜像在 S1 执行 `sudo docker login ghcr.io -u Han-B1ng`，在交互 Password 提示中输入具有 read:packages 的个人访问令牌；不要将令牌写在命令参数或聊天中。若用户决定公开镜像，需在 GitHub Packages 对三份包设置 Public，再验证匿名拉取。

在 Mac 下载成功运行的配置附件：

```bash
gh run download <run-id> --repo Han-B1ng/personal-economy --name preview-deploy-<run-id>-<attempt> --dir .cache/server-preview-<run-id>
COPYFILE_DISABLE=1 tar -czf .cache/server-preview-<run-id>.tar.gz -C .cache/server-preview-<run-id> .
scp .cache/server-preview-<run-id>.tar.gz ubuntu@100.79.89.116:/tmp/
```

也可在 GitHub Actions 运行详情下载附件。附件只有配置，不含镜像；不要把旧的不完整大包继续用于安装。

### G2. 首次部署（用户在 S1 执行）

先检查 `ss -lntp`、`tailscale serve status` 和 `/opt/personal-economy`。上一轮失败解包可能留下部分文件：先检查有无 `.env`、`secrets`、容器及数据库卷，不要直接覆盖。若确认仅是未启动的失败解包目录，可由操作者将旧目录改名留存，再建立新的目录；有已有数据库/凭据时按更新步骤处理。

在确认新目录可用后：

```bash
sudo mkdir /opt/personal-economy
sudo tar -xzf /tmp/server-preview-<run-id>.tar.gz -C /opt/personal-economy
cd /opt/personal-economy
sha256sum -c SHA256SUMS
sudo cp .env.example .env
sudo docker compose -f compose.preview.yaml config --quiet
sudo docker compose -f compose.preview.yaml pull
sudo python3 init-preview.py
sudo docker compose -f compose.preview.yaml up -d --wait --wait-timeout 300
sudo docker compose -f compose.preview.yaml ps
```

先 pull 再初始化：这样不会因镜像不可达而留下已生成但未使用的凭据。确认 `.env` 中 HTTPS Origin 与实际入口一致。配置校验不证明服务能启动，容器健康不代表完整业务验收。

新目录生成凭据后，给其他主机用户限制配置读取权限：

```bash
sudo chmod 700 /opt/personal-economy
```

此后非 root 操作者检查文件需加 sudo。Docker 中的文件挂载仍能读取指定 secret。

确认入口401、启动状态和资源占用后，按本文第5节由用户开启独立 Tailscale 8443入口并验收。现有443与 Hermes保持原样。

### G3. 后续版本更新

先按备份章节做数据库备份并保管 secrets。下载指定成功运行的配置附件，核对 release.json 与 SHA256SUMS，保留旧部署配置。更新 Compose（新附件固定新 digest），保留 `.env` 的入口和用户名及现有 secrets，不重新生成数据库密码；同步更新 `.env` 的 PE_IMAGE_PREFIX 与 RELEASE_TAG，然后手动 pull 与 up。不要直接把新 `.env.example` 覆盖为现有 `.env`。

停服务不会删除数据库卷。镜像回滚使用保留的旧 Compose 配置；迁移兼容性需单独审核，不声称换回镜像即可回滚数据库。

## 离线备用方式

下列方式保留用于镜像仓库不可达时，由操作者明确选择后使用。

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
