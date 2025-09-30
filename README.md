# Dev quick start
1) Install Docker Desktop
2) Clone repo
3) Windows:   .\scripts\dev-up.ps1
   mac/linux: make up

# URLs
- Keycloak:  http://keycloak.localhost  (admin/admin123)
- Nextcloud: http://nc.localhost        (first run: create admin)
- OnlyOffice:http://onlyoffice.localhost
- Portal:    http://localhost:8080

# Common cmds
docker compose ps
docker compose logs -f keycloak
docker compose down -v  # reset all volumes (careful)

# host set
C:\Windows\System32\drivers\etc\hosts <-- 관리자 권한으로 실행 \
127.0.0.1 keycloak.localhost nc.localhost onlyoffice.localhost traefik.localhost
