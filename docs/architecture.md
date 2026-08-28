# RouteMate 유지보수 구조

## 도메인 모듈

`user`, `destination`, `plan`, `product`, `partner`, `admin`, `lotto`는 기능 단위 모듈이다.
`common`과 `global`은 모든 모듈이 사용할 수 있는 기술 공통 영역으로만 사용한다.

각 도메인 모듈은 다음 책임을 가진다.

| 모듈 | 책임 |
| --- | --- |
| `user` | 회원, 로그인 세션, 사용자 프로필 |
| `destination` | 국가, 지역, 여행지, 추천 여행지 |
| `plan` | 여행 계획, 일차, 일정, 준비물 |
| `product` | 옵션상품, 상품 옵션, 주문 |
| `partner` | 파트너사와 파트너 직원, 파트너 포털 |
| `admin` | 운영자 권한과 운영 화면용 유스케이스 |
| `lotto` | 외부 로또 API 연동 및 번호 통계 |

## 코드 작성 규칙

1. 컨트롤러는 HTTP 입출력과 인증 확인만 담당하고, 조회·변경 로직은 서비스로 이동한다.
2. 다른 도메인에 노출할 DTO를 재사용하지 않는다. 예를 들어 파트너 API는 `admin.dto`가 아니라 `partner.dto`를 사용한다.
3. 다른 모듈의 JPA Repository를 직접 주입하는 의존성은 최소화한다. 필요한 경우 해당 모듈의 조회/명령 서비스를 통해 접근한다.
4. 외부 HTTP 호출은 `client` 패키지에 두고, 재시도·타임아웃·오류 변환을 클라이언트에 집중한다.
5. 공개 조회는 짧은 TTL 캐시를 사용할 수 있지만, 관리자 변경 직후 최신성이 반드시 필요한 데이터는 캐시 제거 또는 캐시 미사용을 선택한다.

## 현재 구조 점검 결과

Spring Modulith로 점검한 결과 `admin`, `partner`, `product`, `user`, `plan` 사이에 순환 의존성이 있다.
특히 파트너 포털이 `admin.dto`와 `AdminAuthorizationService`를 직접 사용하고, 관리자가 파트너·상품 Repository를 직접 사용하는 부분이 원인이다.

진행 상태와 다음 순서는 다음과 같다.

1. **완료**: `AuthorizationService` 계약을 `common.security`로 분리했다. `user`와 `partner`는 더 이상 `admin.service`를 직접 참조하지 않는다.
2. **완료**: 파트너 상품 등록/수정 요청·응답을 `partner.dto`로 분리했다. 파트너 포털은 더 이상 `admin.dto`를 사용하지 않는다.
3. **다음**: `TravelProduct`의 파트너 소유 관계와 파트너 포털의 상품 관리 책임을 `commerce` 성격의 공용 모듈 또는 명시적 port로 분리한다.
4. **다음**: 관리자 대시보드처럼 여러 도메인을 읽는 기능은 `admin`의 read-model로만 제한한다.
5. **마지막**: 모듈 검증 테스트를 다시 활성화해 새 순환 의존성이 생기지 않도록 한다.

## 데이터베이스 규칙

- Flyway는 `db/migration/mysql`과 `db/migration/oracle`의 버전 번호를 동일하게 유지한다.
- 이미 적용된 마이그레이션은 수정하지 않고 다음 버전을 추가한다.
- Oracle 전용 SQL은 `MERGE`, `DATE 'YYYY-MM-DD'`, `VARCHAR2`, `NUMBER` 등 Oracle 문법으로 작성한다.
- MySQL 전용 구문(`ON DUPLICATE KEY UPDATE`, `AUTO_INCREMENT`, `CONCAT`)은 Oracle migration에 넣지 않는다.

## 검증 단계

```bash
./gradlew test
cd frontend && npm run build
```

Testcontainers 의존성은 실제 MySQL 통합 테스트를 추가할 준비를 위한 것이며, Docker가 없는 개발 환경의 기본 테스트 실행에는 관여하지 않는다.
