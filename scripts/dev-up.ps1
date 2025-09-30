# 복사해서 .env 생성(이미 있으면 건너뜀)
if (-not (Test-Path ../.env)) { Copy-Item ../.env.example ../.env }
docker compose -f ../docker/docker-compose.yml pull
docker compose -f ../docker/docker-compose.yml up -d
Write-Host "Open: http://keycloak.localhost , http://nc.localhost , http://onlyoffice.localhost"
