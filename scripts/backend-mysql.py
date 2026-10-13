#!/usr/bin/env python3
"""Load project database settings without executing .env content or exposing passwords."""
import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
env = os.environ.copy()
values = {}
config = root / '.env'
if config.exists():
    for line in config.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith('#'):
            continue
        key, separator, value = line.partition('=')
        if not separator:
            raise SystemExit('Invalid .env line: expected KEY=VALUE')
        values[key.strip()] = value.strip().strip('\"\'')

def setting(name, default=''):
    return env.get(name, values.get(name, default))

env.setdefault('DB_USERNAME', setting('MYSQL_USER', 'personal_economy'))
env.setdefault('DB_PASSWORD', setting('MYSQL_PASSWORD'))
if not env['DB_PASSWORD']:
    raise SystemExit('Set MYSQL_PASSWORD in root .env or DB_PASSWORD in the environment.')
database = setting('MYSQL_DATABASE', 'personal_economy')
port = setting('MYSQL_PORT', '3306')
if not database.replace('_', '').isalnum() or not port.isdigit():
    raise SystemExit('Invalid MYSQL_DATABASE or MYSQL_PORT')
env.setdefault('DB_URL', f'jdbc:mysql://127.0.0.1:{port}/{database}?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true')
env.setdefault('SPRING_PROFILES_ACTIVE', 'dev')
env.setdefault('PE_DATA_FILE', str(root / 'data' / 'workspace.json'))
if len(sys.argv) > 1:
    # For a prebuilt, immutable jar: python3 scripts/backend-mysql.py path/to.jar
    java_home = env.get('JAVA_HOME')
    if Path('/usr/libexec/java_home').exists():
        java_home = subprocess.check_output(['/usr/libexec/java_home', '-v', '17'], text=True).strip()
    java = str(Path(java_home) / 'bin' / 'java') if java_home else 'java'
    os.execve(java, [java, '-jar', str(Path(sys.argv[1]).resolve())], env)
else:
    command = root / 'scripts' / 'backend.sh'
    os.execve(str(command), [str(command), 'spring-boot:run'], env)
