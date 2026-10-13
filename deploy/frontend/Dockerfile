FROM caddy:2-alpine
COPY frontend/dist/ /srv/
COPY deploy/frontend/Caddyfile /etc/caddy/Caddyfile
COPY deploy/frontend/entrypoint.sh /usr/local/bin/pe-entrypoint
ENTRYPOINT ["/bin/sh", "/usr/local/bin/pe-entrypoint"]
