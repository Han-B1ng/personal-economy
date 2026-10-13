#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -x /usr/libexec/java_home ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
fi
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! "$java_bin" -version 2>&1 | head -n 1 | grep -Eq 'version "17\.'; then
  echo '请安装 JDK 17，并将 JAVA_HOME 指向该版本。' >&2
  exit 1
fi
export MAVEN_USER_HOME="$project_root/.cache/maven-user"
export PE_DATA_FILE="${PE_DATA_FILE:-$project_root/data/workspace.json}"
cd "$project_root/backend"
exec ./mvnw -B -ntp "-Dmaven.repo.local=$project_root/.cache/maven" "$@"
