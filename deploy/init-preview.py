#!/usr/bin/env python3
"""Initialize private preview secrets once. Run manually on S1, never in a build."""
import getpass
import os
from pathlib import Path
import re
import secrets
import subprocess
import tempfile

root = Path(__file__).resolve().parent
settings = {}
for line in (root / ".env").read_text().splitlines():
    if line.strip() and not line.lstrip().startswith("#"):
        key, value = line.split("=", 1)
        settings[key.strip()] = value.strip()
tag = settings.get("RELEASE_TAG", "")
if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]{0,63}", tag) or tag == "CHANGE_ME":
    raise SystemExit("请先在 .env 中设置发布包对应的 RELEASE_TAG")
prefix = settings.get("PE_IMAGE_PREFIX", "personal-economy")
if not re.fullmatch(r"[a-z0-9][a-z0-9./:_-]*", prefix):
    raise SystemExit("PE_IMAGE_PREFIX 格式无效")
directory = root / "secrets"
if directory.exists():
    raise SystemExit("secrets 已存在；为避免数据库密码失配，本脚本不会覆盖。请核对现有文件。")
password = getpass.getpass("设置测试入口密码（至少16个字符，不显示）：")
if len(password) < 16 or len(password.encode()) > 72:
    raise SystemExit("密码至少16字符，且UTF-8编码不超过72字节")
if password != getpass.getpass("再输入一次："):
    raise SystemExit("两次密码不同，未创建文件")
result = subprocess.run([
    "docker", "run", "--rm", "-i", "--network", "none", "--entrypoint", "caddy",
    f"{prefix}/frontend:{tag}", "hash-password"
], input=password + "\n", text=True, capture_output=True, check=True)
hashed = result.stdout.strip()
if not hashed.startswith(("$2a$", "$2b$", "$2y$")):
    raise SystemExit("入口密码哈希生成失败；未创建文件")
values = {"DB_PASSWORD": secrets.token_hex(32),
          "MYSQL_ROOT_PASSWORD": secrets.token_hex(32),
          "PE_PREVIEW_TOKEN": secrets.token_hex(32),
          "PREVIEW_PASSWORD_HASH": hashed}
os.umask(0o077)
with tempfile.TemporaryDirectory(prefix=".secrets-", dir=root) as temporary:
    staged = Path(temporary) / "secrets"
    staged.mkdir(mode=0o700)
    for name, value in values.items():
        target = staged / name
        target.write_text(value)
        # Docker bind-mounted secrets must be readable by the non-root Java process.
        # Host access remains restricted by the parent directory's 0700 permissions.
        target.chmod(0o444)
    staged.rename(directory)
print("已生成凭据。入口用户名见 .env；密码是你刚输入的密码。请保管 secrets 目录。")
