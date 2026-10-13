#!/bin/sh
set -eu
export PE_PREVIEW_TOKEN="$(cat /run/secrets/PE_PREVIEW_TOKEN)"
export PREVIEW_PASSWORD_HASH="$(cat /run/secrets/PREVIEW_PASSWORD_HASH)"
exec caddy run --config /etc/caddy/Caddyfile --adapter caddyfile
