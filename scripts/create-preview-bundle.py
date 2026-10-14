#!/usr/bin/env python3
"""Produce a credential-free config bundle only after all CI images are published."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil

root = Path(__file__).resolve().parent.parent
prefix = os.environ["PE_IMAGE_PREFIX"]
tag = os.environ["PE_RELEASE_TAG"]
if not re.fullmatch(r"ghcr\.io/[a-z0-9_.-]+/[a-z0-9_./-]+", prefix):
    raise SystemExit("Invalid GHCR image prefix")
if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]{0,127}", tag):
    raise SystemExit("Invalid image tag")
folder = root / ".cache/preview-bundle"
folder.mkdir(parents=True, exist_ok=False)
compose = (root / "deploy/compose.preview.yaml").read_text()
images = {}
for service in ("backend", "frontend", "mysql"):
    digest = os.environ[f"{service.upper()}_DIGEST"]
    if not re.fullmatch(r"sha256:[a-f0-9]{64}", digest):
        raise SystemExit("Missing or invalid published image digest")
    images[service] = f"{prefix}/{service}@{digest}"
    # Pin exact image content for this artifact; tags remain useful for operator traceability.
    compose = compose.replace(
        '${PE_IMAGE_PREFIX:-personal-economy}/' + service + ':${RELEASE_TAG:?Set RELEASE_TAG}',
        images[service])
(folder / "compose.preview.yaml").write_text(compose)
for name in ("init-preview.py", "README.md"):
    shutil.copyfile(root / "deploy" / name, folder / name)
settings = (root / "deploy/.env.example").read_text()
settings = settings.replace("PE_IMAGE_PREFIX=personal-economy", "PE_IMAGE_PREFIX=" + prefix)
settings = settings.replace("RELEASE_TAG=CHANGE_ME", "RELEASE_TAG=" + tag)
(folder / ".env.example").write_text(settings)
# The initializer uses the same digest as Compose, avoiding a changed tag during hashing.
(folder / "init-preview.py").write_text((folder / "init-preview.py").read_text().replace(
    'f"{prefix}/frontend:{tag}"', repr(images["frontend"])))
(folder / "release.json").write_text(json.dumps({"commit": os.environ.get("GITHUB_SHA"),
    "release_tag": tag, "images": images}, indent=2) + "\n")
lines = []
for path in sorted(folder.iterdir()):
    lines.append(hashlib.sha256(path.read_bytes()).hexdigest() + "  " + path.name)
(folder / "SHA256SUMS").write_text("\n".join(lines) + "\n")
print("Created credential-free deployment bundle:", folder)
