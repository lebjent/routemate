# 🗺️ RouteMate (루트메이트)

RouteMate는 스마트한 여행 동선을 설계하고 관리할 수 있는 웹 애플리케이션입니다. 사용자 친화적인 인터페이스와 안정적인 백엔드 시스템을 바탕으로 여행 계획을 손쉽게 세울 수 있도록 돕습니다.

## 🛠 기술 스택 (Tech Stack)

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.3
- **Security**: Spring Security (세션 기반 인증, BCrypt 암호화)
- **Database**: Oracle DB (ojdbc11) / H2 Database (개발 및 테스트용)
- **ORM**: Spring Data JPA, Hibernate
- **Tools**: Lombok, Gradle

### Frontend
- **Framework**: React 18
- **Language**: TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS (유리 질감(Glassmorphism) 및 반응형 UI 디자인)
- **Routing**: React Router DOM
- **HTTP Client**: Axios

---

## ✨ 주요 기능 (Key Features)

### 1. 사용자 인증 및 인가 (Authentication)
- **Spring Security**를 활용한 안전한 세션 기반 로그인 및 로그아웃
- 사용자 권한 분리 및 보호된 API 라우팅 처리
- 프론트엔드 비동기 통신(Axios)과 스프링 시큐리티 간의 CORS 및 CSRF 예외 처리 완료
- 로그인 상태를 전역으로 관리하는 `AuthContext` 구현

### 2. 고도화된 회원가입 시스템 (Sign Up)
- **실시간 중복 검사**: 
  - 이메일 입력 시 정규식 패턴 검사 및 실시간 서버 통신(`onBlur`)을 통한 중복 체크
  - 닉네임 입력 시 실시간 서버 통신을 통한 중복 체크
- **직관적인 UI/UX**: 
  - 중복 여부 및 양식 오류에 따라 실시간으로 테두리 색상(Red/Green) 및 안내 아이콘/메시지 피드백 제공
- **비밀번호 보안성 검사**: 입력된 비밀번호의 조합(대소문자, 숫자, 특수문자 등)에 따라 실시간 보안 등급(매무 약함 ~ 매우 강함) 시각화
- **주소 검색 연동**: 다음(Daum) 우편번호 서비스 API를 연동하여 편리한 주소 및 우편번호 입력 지원
- **커스텀 달력**: 생년월일 입력을 위한 자체 제작 달력 컴포넌트(`CustomCalendar`) 지원

### 3. 메인 화면 및 여행 데이터 (Home & Destinations)
- 여행지 정보(`tb_destination`) 및 유저들의 여행 계획(`tb_travel_plan`) 리스트 제공
- 좋아요(`like_count`) 기반의 인기 여행지 및 플랜 정렬 기능 지원
- 서버 초기 구동 시 `DataInitializer`를 통한 기본 데이터(더미 데이터) 자동 세팅

### 4. 부가 기능
- 로또 번호 추첨기 (`/lotto`) 등 미니 기능 포함

---

## 🚀 최근 업데이트 내역 (Recent Updates)
- [x] **회원가입 폼 UX 개선**: 이메일 주소 정규식 검증 기능 추가 및 에러 메시지 UI 노출
- [x] **API 엔드포인트 우회 설정**: 비로그인 사용자가 `/api/auth/me` 호출 시 발생하는 403 Forbidden 에러를 방지하기 위해 `SecurityConfig` 수정 및 빈 객체 응답 처리
- [x] **닉네임 중복 체크 API 구현**: `UserMstrRepository` 및 `UserService`에 로직을 추가하고 프론트엔드 `Join.tsx`와 연동
- [x] **이메일 중복 체크 API 구현**: 닉네임과 동일한 방식으로 실시간 이메일 중복 체크 로직 적용

---

## 💻 실행 방법 (How to Run)

### Backend
1. IntelliJ에서 프로젝트를 엽니다.
2. `RoutemateApplication.java`를 실행하거나 터미널에서 `./gradlew bootRun`을 실행합니다. (기본 포트: `8090`)

### Frontend
1. 터미널에서 `frontend` 디렉토리로 이동합니다. (`cd frontend`)
2. 패키지를 설치합니다. (`npm install`)
3. 개발 서버를 실행합니다. (`npm run dev`)
4. `http://localhost:5173` 으로 접속하여 확인합니다.
5. (운영 배포 시) `npm run build`를 통해 정적 파일을 백엔드의 `src/main/resources/static` 폴더로 빌드합니다.
