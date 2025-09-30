# AMGW – Dev Stack & SSO-ready Portal

로컬에서 **Keycloak + Nextcloud + ONLYOFFICE + Traefik**을 한 번에 띄우고, 스프링 포털(AMGW)을 **OIDC 로그인/로그아웃**으로 테스트할 수 있는 개발 환경입니다.

---

## 요구사항

* Windows 10/11 + **Docker Desktop(WSL2)**
  *(macOS/Linux도 Docker 있으면 동작)*
* Git, **JDK 17+** (포털 실행용)
* **hosts**(관리자 권한으로 1회 편집)

  ```
  127.0.0.1 keycloak.localhost nc.localhost onlyoffice.localhost traefik.localhost
  ```

---

## 레포 구조

```
AMGW/
├─ docker/                      # Dev 스택 (Traefik, Keycloak, Nextcloud, OnlyOffice, DB)
│  ├─ docker-compose.yml
│  ├─ reverse-proxy/traefik_dynamic.yml
│  └─ .env.example             # 복사해서 .env 로 사용(커밋 금지)
├─ scripts/
│  ├─ dev-up.ps1               # Windows 원클릭 기동
│  └─ dev-down.ps1             # Windows 종료
└─ src/...                     # Spring Boot Portal (AMGW)
```

> ⚠️ `docker/.env`는 **커밋 금지**. 항상 `docker/.env.example`만 커밋하세요.

---

## 빠른 시작

### Windows (권장)

```powershell
# 1) 클론
cd C:\dev
git clone https://github.com/torye2/AMGW.git
cd AMGW\scripts

# 2) Dev 스택 기동 (최초 실행 시 .env.example → .env 자동 복사)
.\dev-up.ps1

# 3) 상태 확인
cd ..\docker
docker compose ps
```

### macOS / Linux

```bash
git clone https://github.com/torye2/AMGW.git
cd AMGW/docker
cp .env.example .env
docker compose up -d
```

접속 URL

* Traefik:  `http://traefik.localhost`
* Keycloak: `http://keycloak.localhost`
* Nextcloud: `http://nc.localhost`  *(최초 1회 관리자 계정 생성)*
* ONLYOFFICE: `http://onlyoffice.localhost`
* 포털(Spring): `http://localhost:8080`

---

## Nextcloud 연동 (최초 1회만)

1. **관리자 생성**: `http://nc.localhost`

   * DB: **MySQL/MariaDB** 선택
   * DB 호스트: `nextcloud-db:3306`
   * DB 이름/사용자/암호: compose 기본값(예: `nextcloud` / `nextcloud` / `ncpass123`)
2. **ONLYOFFICE 연결**

   * Apps → **ONLYOFFICE** 설치
   * Settings → **ONLYOFFICE** → Document Server: `http://onlyoffice.localhost` 저장
   * 새 문서 생성 시 브라우저에서 편집기 뜨면 OK
3. **(선택) Keycloak OIDC SSO**

   * 앱: **OpenID Connect Login**(단순) *또는* **Social Login**(멀티)
   * Issuer: `http://keycloak.localhost/realms/gw`
   * Client ID: `nextcloud`
   * Redirect URI (앱에 따라 택1)

     * OpenID Connect Login: `http://nc.localhost/apps/oidc_login/redirect`
     * Social Login(Custom OIDC): `http://nc.localhost/index.php/apps/sociallogin/custom_oidc/gw`
   * Keycloak(Clients → `nextcloud`)에 위 **Redirect URI**와 **Web origins: `http://nc.localhost`** 등록

---

## 포털(AMGW) 실행 & SSO 확인

1. IntelliJ로 프로젝트 열고 Spring Boot 실행
2. `http://localhost:8080/health` → `{ "ok": "true" }`
3. `http://localhost:8080/me` → **Keycloak 로그인** → 사용자 클레임 JSON 확인
4. `/admin/ping`은 **ADMIN** 롤 필요(예시)

### 로그아웃 (RP-initiated)

* `/logout` 호출 시 **Keycloak 세션까지 종료** 후 `{baseUrl}/` 또는 `{baseUrl}/me`로 복귀
* Keycloak `portal` 클라이언트에 **Valid Post Logout Redirect URIs**: `http://localhost:8080/*` 등록 필요

---

## 환경 변수 (요약)

`docker/.env.example`에서 필요한 값만 수정 후 `.env`로 사용:

```dotenv
TZ=Asia/Seoul
DOMAIN=localhost
PROXY_HTTP_PORT=80              # 80 충돌 시 8081 등으로 변경
MYSQL_HOST_PORT=3307            # 팀원별로 달리 써도 OK

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://keycloak-db:5432/keycloak
KC_DB_USERNAME=keycloak
KC_DB_PASSWORD=keycloak123
KC_HOSTNAME=keycloak.${DOMAIN}

# Nextcloud
NEXTCLOUD_DB_PASS=ncpass123
NEXTCLOUD_HOST=nc.${DOMAIN}

# OnlyOffice
ONLYOFFICE_HOST=onlyoffice.${DOMAIN}
```

---

## 종료/정리

* **Windows**

  ```powershell
  .\scripts\dev-down.ps1
  ```
* **macOS/Linux**

  ```bash
  cd docker
  docker compose down
  ```
* **초기화(데이터 삭제 주의)**

  ```bash
  docker compose down -v
  ```

---

## 자주 겪는 이슈 & 해결

* **포트 80 충돌** → `.env`의 `PROXY_HTTP_PORT`를 8081 등으로 변경 후 재기동.
  접속 시 `http://traefik.localhost:8081` 사용.
* **Nextcloud DB 연결 실패** → DB 호스트는 **`nextcloud-db:3306`** (로컬 `localhost:3307` 아님).
* **SSO `invalid_redirect_uri`** → Keycloak 클라이언트의 Redirect/Web origins 문자열이 정확히 일치하는지 확인.
* **로그아웃 후 자동 로그인** → RP-initiated logout 설정 + Keycloak **Post Logout URI** 등록.
* **도메인 경고**(Nextcloud) → 필요 시:

  ```bash
  docker exec -u www-data -it docker-nextcloud-1 php occ config:system:set trusted_domains 1 --value=nc.localhost
  ```

---

## 트러블슈팅 명령어

```bash
# 상태/로그
docker compose ps
docker compose logs -f reverse-proxy
docker compose logs -f keycloak
docker compose logs -f nextcloud
docker compose logs -f onlyoffice

# 전체 재기동
docker compose down && docker compose up -d
```

---

문의/이슈는 GitHub **Issues**에 남겨 주세요.
팀 온보딩 시엔 `docker compose ps`와 에러 로그 몇 줄을 함께 공유하면 가장 빠르게 해결됩니다.
