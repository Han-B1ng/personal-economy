#!/usr/bin/env bash
# Build an offline Linux AMD64 deployment package. Does not connect to any server.
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
release_tag="${1:-$(date +%Y%m%d-%H%M%S)}"
if [[ ! "$release_tag" =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]{0,63}$ ]]; then
  echo '发布标签格式无效' >&2; exit 1
fi
release_dir="$project_root/.cache/releases/$release_tag"
if [[ -e "$release_dir" ]]; then
  echo '发布目录已经存在，请换一个标签，避免覆盖' >&2; exit 1
fi
command -v docker >/dev/null
command -v python3 >/dev/null
docker info >/dev/null
cd "$project_root"
npm --prefix frontend ci
VITE_HOME_DATA_SOURCE=api VITE_API_BASE_URL=/api/v1 npm --prefix frontend run build
bash scripts/backend.sh clean package -DskipTests
for service in backend frontend mysql; do
  docker build --platform linux/amd64 -f "deploy/$service/Dockerfile" \
    -t "personal-economy/$service:$release_tag" .
done
mkdir -p "$release_dir/bundle"
cp deploy/compose.preview.yaml deploy/init-preview.py deploy/README.md "$release_dir/bundle/"
python3 - "$release_tag" "$release_dir/bundle/.env.example" <<'PYENV'
from pathlib import Path
import sys
Path(sys.argv[2]).write_text(Path('deploy/.env.example').read_text().replace('RELEASE_TAG=CHANGE_ME', 'RELEASE_TAG=' + sys.argv[1]))
PYENV
docker image save "personal-economy/backend:$release_tag" \
  "personal-economy/frontend:$release_tag" "personal-economy/mysql:$release_tag" \
  | gzip > "$release_dir/bundle/images.tar.gz"
python3 - "$release_dir/bundle" <<'PYHASH'
import hashlib
from pathlib import Path
import sys
folder = Path(sys.argv[1])
lines = []
for path in sorted(folder.iterdir()):
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(block)
    lines.append(f'{digest.hexdigest()}  {path.name}')
(folder / 'SHA256SUMS').write_text('\n'.join(lines) + '\n')
PYHASH
tar -czf "$release_dir/personal-economy-$release_tag.tar.gz" -C "$release_dir/bundle" .
echo "发布包：$release_dir/personal-economy-$release_tag.tar.gz"
echo "RELEASE_TAG=$release_tag"
echo '仅完成本地打包，没有上传或启动服务器。测试套件未执行。'
