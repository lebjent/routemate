# RouteMate

RouteMate는 여행 일정과 여행지 정보를 관리하고, 여행지 기반의 옵션 상품을 판매하는 여행 플랫폼입니다. 일반 사용자는 여행 계획을 만들고 상품을 주문할 수 있으며, 관리자는 여행지·추천·상품·파트너사·직원·회원 데이터를 하나의 관리자 화면에서 운영할 수 있습니다.

## 주요 기능

### 사용자 서비스

- 이메일/비밀번호 회원가입, 로그인, 로그아웃, 로그인 사용자 조회
- 이메일·닉네임 중복 확인 및 비밀번호 재설정
- 국가, 지역, 여행지와 여행지 추천 콘텐츠 조회
- 여행 일정 생성·수정·조회
  - 여행 일차별 방문 지역
  - 일정/교통편
  - 준비물
  - 공개 여행 일정 상세 페이지
- 옵션 상품 카탈로그 및 상품 상세 페이지
- 상품 옵션 선택과 주문, 내 상품 주문 내역 조회
- 로또 번호 생성 및 번호 빈도 조회
- 모든 일반 사용자용 `select` 입력을 공통 UI로 통일

### 상품·파트너 운영

- 여행 옵션 상품 등록·수정·목록 관리
- 상품 기본 정보, 상세 설명, 설명 이미지 URL, 판매 상태, 가격 관리
- 상품 옵션(옵션명, 설명, 가격, 재고, 판매 여부) 관리
- 파트너사 등록·수정·승인 상태 관리
- 상품을 파트너사와 연결하고 등록 출처(관리자/파트너) 구분
- 공개 상품은 승인된 상품 및 활성 파트너사만 노출

### 관리자 서비스

`/admin/login`에서 관리자 세션으로 로그인합니다. 역할과 권한에 따라 메뉴 및 API 접근이 제한됩니다.

- 대시보드: 현재 등록된 회원, 여행지, 상품, 파트너사 및 주문 현황
- 회원 관리: 회원 목록, 상태 변경
- 직원 관리: 직원 등록, 역할 변경, 활성 상태 변경
- 여행지 관리: 국가, 지역, 장소, 장소 카테고리 관리
- 추천 관리: 여행지 추천 콘텐츠 등록·수정
- 상품 관리: 상품 및 옵션 등록·수정, 승인/노출 상태 관리
- 파트너사 관리: 파트너사 등록, 승인 상태 변경, 상품 연결
- 관리자 역할별 메뉴/권한 정책

## 기술 스택

### Backend

- Java 21
- Spring Boot 3.4.3
- Spring MVC, Spring Data JPA, Hibernate
- Spring Security 세션 기반 인증 및 BCrypt 비밀번호 해시
- Flyway 데이터베이스 마이그레이션
- Caffeine Cache, Resilience4j 재시도, Micrometer Prometheus 운영 메트릭
- Testcontainers 및 Spring Modulith Test (통합 테스트·모듈 경계 검증 기반)
- Gradle
- 테스트: JUnit 5, Spring Boot Test, Spring Security Test

### Frontend

- React 19 + TypeScript
- Vite
- React Router
- Axios
- Tailwind CSS
- Lucide React 아이콘
- React Datepicker 및 React PDF

### Database

- Oracle 12c 이상 (기본 프로필)
- MySQL 8.x
- H2 메모리 DB (테스트 프로필)

Oracle과 MySQL은 identity column, CLOB, timestamp 기본값, index 문법 차이 때문에 별도의 Flyway migration 디렉터리를 사용합니다.

## 프로젝트 구조

```text
routeMate/
├─ src/main/java/com/trip/routemate/
│  ├─ user/                 # 회원·인증
│  ├─ destination/          # 국가·지역·여행지·추천
│  ├─ plan/                 # 여행 계획
│  ├─ product/              # 상품·옵션·주문
│  ├─ partner/              # 파트너사
│  ├─ admin/                # 관리자 API·권한·서비스
│  └─ lotto/                # 로또 부가 기능
├─ src/main/resources/
│  ├─ db/migration/common/  # 공통 migration
│  ├─ db/migration/oracle/  # Oracle migration
│  ├─ db/migration/mysql/   # MySQL migration
│  └─ application*.yml     # 프로필별 설정
├─ frontend/src/
│  ├─ pages/                # 사용자·관리자 화면
│  ├─ components/           # 공통 UI
│  ├─ contexts/             # 인증 상태
│  └─ features/             # 도메인별 프론트 모델·훅
└─ docs/sql/                # 조회·인덱스 참고 SQL
```

도메인 책임, 모듈 의존성 개선 순서, Oracle/MySQL migration 규칙은 [구조 문서](docs/architecture.md)를 참고합니다.

## 실행 전 준비

- JDK 21
- Gradle Wrapper 사용 권장
- Oracle 또는 MySQL 실행
- 기본 데이터베이스 접속 정보가 실제 환경과 다르면 환경 변수로 재정의

기본 설정은 다음과 같습니다.

| 프로필 | 기본 DB | 기본 포트 |
| --- | --- | --- |
| `oracle` | `jdbc:oracle:thin:@localhost:1521/ORCL` | 8090 |
| `mysql` | `jdbc:mysql://localhost:3306/routemate` | 8090 |
| `test` | H2 메모리 DB | 8090 |

기본 DB 계정은 `routemate`/`1234`로 설정되어 있으며, 운영 환경에서는 반드시 변경해야 합니다.

## Backend 실행

Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
$env:SPRING_PROFILES_ACTIVE = "oracle"
$env:SPRING_DATASOURCE_USERNAME = "routemate"
$env:SPRING_DATASOURCE_PASSWORD = "1234"
./gradlew.bat bootRun
```

macOS/Linux:

```bash
export JAVA_HOME=/path/to/jdk-21
export SPRING_PROFILES_ACTIVE=oracle
export SPRING_DATASOURCE_USERNAME=routemate
export SPRING_DATASOURCE_PASSWORD=1234
./gradlew bootRun
```

MySQL을 사용하려면 `SPRING_PROFILES_ACTIVE=mysql`로 실행합니다.

```bash
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun
```

Flyway는 애플리케이션 시작 시 활성 프로필의 migration을 자동 검증·적용합니다. 이미 적용된 migration 파일은 수정하지 말고 반드시 새 버전을 추가해야 합니다.

## Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

개발 서버는 `http://localhost:5173`에서 실행되고 `/api` 요청은 `http://localhost:8090`으로 프록시됩니다.

운영용 정적 파일은 다음 명령으로 생성합니다. Vite 출력 경로는 백엔드의 `src/main/resources/static`으로 설정되어 있습니다.

```bash
cd frontend
npm run build
```

백엔드 실행 후 `http://localhost:8090`에서도 동일한 React 화면을 사용할 수 있습니다.

## 주요 화면 경로

| 경로 | 설명 |
| --- | --- |
| `/` | 홈 및 여행지·추천 콘텐츠 |
| `/join` | 회원가입 |
| `/login` | 사용자 로그인 |
| `/products` | 옵션 상품 목록 |
| `/products/:productId` | 상품 상세·옵션 선택·주문 |
| `/my-product-orders` | 내 상품 주문 내역 |
| `/my-trips` | 내 여행 계획 |
| `/my-trips/new` | 여행 계획 작성 |
| `/travel-plans/:planId` | 공개 여행 계획 |
| `/lotto` | 로또 기능 |
| `/admin/login` | 관리자 로그인 |
| `/admin` | 관리자 대시보드 |
| `/admin/products` | 상품 관리 |
| `/admin/partners` | 파트너사 관리 |
| `/admin/destinations` | 여행지 관리 |
| `/admin/recommendations` | 추천 관리 |
| `/admin/users` | 회원 관리 |
| `/admin/staff` | 직원·역할 관리 |

## 주요 API 그룹

| API prefix | 기능 |
| --- | --- |
| `/api/auth`, `/api/user` | 사용자 인증·회원 |
| `/api/destinations` | 공개 국가·지역 조회 |
| `/api/public/travel-plans` | 공개 여행 계획 |
| `/api/my-travel-plans` | 로그인 사용자 여행 계획 CRUD |
| `/api/public/products` | 공개 상품 목록·상세 |
| `/api/product-orders` | 상품 주문·내 주문 |
| `/api/admin/dashboard` | 관리자 대시보드 |
| `/api/admin/users` | 회원 관리 |
| `/api/admin/staff` | 직원 관리 |
| `/api/admin/destinations` | 국가·지역·장소 관리 |
| `/api/admin/recommendations` | 추천 관리 |
| `/api/admin/products` | 상품·옵션 관리 |
| `/api/admin/partners` | 파트너사 관리 |
| `/api/lotto` | 로또 번호·빈도 |

## API 문서 및 런타임 매핑

Springdoc OpenAPI와 Spring Boot Actuator를 함께 사용합니다.

| 경로 | 용도 | 접근 정책 |
| --- | --- | --- |
| `/swagger-ui/index.html` | 브라우저용 API 문서·실행 화면 | 공개 |
| `/v3/api-docs` | 전체 OpenAPI JSON | 공개 |
| `/v3/api-docs.yaml` | OpenAPI YAML | 공개 |
| `/v3/api-docs/{group}` | `public`, `user`, `admin` 그룹별 명세 | 공개 |
| `/actuator/health` | 애플리케이션 상태 확인 | 공개 |
| `/actuator/info` | 애플리케이션 정보 | 공개 |
| `/actuator/mappings` | 실제 등록된 Spring request mapping 전체 | 로그인 필요 |

Swagger 문서는 `Public API`, `User API`, `Admin API` 그룹으로 나뉘며, 세션 인증은 `JSESSIONID` 쿠키를 사용합니다. `/actuator/mappings`는 내부 Controller 구조가 노출될 수 있으므로 인증된 사용자만 접근할 수 있습니다.

## 데이터베이스·Flyway 규칙

Migration의 단일 기준은 Flyway입니다.

1. 공통 SQL은 `common`에 둡니다.
2. Oracle/MySQL 문법이 다른 변경은 양쪽 디렉터리에 같은 버전으로 추가합니다.
3. 공유 DB에 적용된 migration은 절대 수정하지 않습니다.
4. 변경마다 새 버전의 migration을 추가합니다.
5. 애플리케이션 시작 전에 DB 백업과 migration 이력을 확인합니다.

현재 파트너사 기능과 공개 상품 조회 인덱스는 `V22`, `V23` migration에 포함되어 있습니다. Flyway가 `Found more than one migration with version ...` 또는 `Validate failed`를 보고하면 migration 버전 중복과 기존 파일 수정 여부를 먼저 확인해야 합니다. 이미 적용된 파일을 되돌리거나 새 migration으로 분리한 뒤 애플리케이션을 재기동합니다.

## 테스트 및 빌드

Backend 테스트:

```bash
./gradlew test
```

오프라인 Gradle 캐시만 사용:

```bash
./gradlew test --offline
```

Frontend 타입 검사 및 프로덕션 빌드:

```bash
cd frontend
npm run build
```

## 환경 변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `oracle`, `mysql`, `test` 중 활성 프로필 | `oracle` |
| `SPRING_DATASOURCE_URL` | JDBC 접속 URL | 프로필별 설정 |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 | `routemate` |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | `1234` |
| `APP_SEED_ENABLED` | 비어 있는 DB에 초기 더미 데이터 삽입 | `false` |
| `SERVER_PORT` | Spring Boot 포트 | `8090` |
| `LOTTO_HISTORY_SOURCE_URL` | 로또 회차 데이터 URL | 동행복권 URL |
| `LOTTO_HISTORY_REFRESH_INTERVAL` | 로또 데이터 갱신 주기 | `12h` |

`APP_SEED_ENABLED=true`는 비어 있는 여행지·여행 계획 테이블에만 초기 데이터를 넣도록 동작합니다. 운영 데이터베이스에서는 데이터 백업 후 신중하게 사용해야 합니다.

## 보안 및 운영 참고

- 기본 비밀번호와 개발용 세션 설정은 운영 배포 전에 변경합니다.
- DB 접속 정보와 외부 API URL은 환경 변수 또는 안전한 비밀 저장소로 관리합니다.
- 관리자 API는 세션과 역할별 권한 검사를 거칩니다.
- 상품은 승인 상태와 파트너사 활성 상태가 모두 유효해야 공개 카탈로그에 노출됩니다.
- `spring.jpa.hibernate.ddl-auto`는 운영에서 `validate`를 유지하고 스키마 변경은 Flyway로 처리합니다.
