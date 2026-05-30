CREATE TABLE TB_USER_MSTR (
    USER_ID             NUMBER GENERATED AS IDENTITY PRIMARY KEY, -- 내부 식별 ID (PK)
    USER_EMAIL          VARCHAR2(100) NOT NULL UNIQUE,            -- 로그인 이메일
    USER_PWD            VARCHAR2(255),                            -- 암호화된 비밀번호
    USER_NICKNM         VARCHAR2(50) NOT NULL,                    -- 닉네임 (활동명)
    SNS_PROVIDER        VARCHAR2(20) DEFAULT 'LOCAL' NOT NULL,    -- 가입 경로 (LOCAL, KAKAO, NAVER)
    SNS_PROVIDER_ID     VARCHAR2(100),                            -- SNS 연동 고유 식별자
    USER_ROLE           VARCHAR2(20) DEFAULT 'USER' NOT NULL,     -- 시스템 권한 (USER, ADMIN)
    USER_STAT_CD        VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,   -- 회원 상태 코드 (ACTIVE, SLEEP, LEAVE)
    DEL_YN              CHAR(1) DEFAULT 'N' NOT NULL,             -- 삭제(탈퇴) 여부 (Y, N)
    JOIN_DT             TIMESTAMP DEFAULT SYSDATE NOT NULL,       -- 가입 일시
    MDFY_DT             TIMESTAMP DEFAULT SYSDATE NOT NULL,       -- 최종 수정 일시

    -- 오라클 데이터 무결성을 위한 제약 조건 정의 (Constraint)
    CONSTRAINT CHK_TB_USER_MSTR_DEL CHECK (DEL_YN IN ('Y', 'N'))
);

-- 인덱스 명명 규칙도 서비스 표준에 맞게 설정 (IDX_[테이블명]_[컬럼명])
CREATE INDEX IDX_TB_USER_MSTR_EMAIL ON TB_USER_MSTR(USER_EMAIL);
CREATE INDEX IDX_TB_USER_MSTR_SNS ON TB_USER_MSTR(SNS_PROVIDER, SNS_PROVIDER_ID);

-- 테이블 및 컬럼 코멘트(주석) 추가 (실무형 DB 설계서 역할을 합니다)
COMMENT ON TABLE TB_USER_MSTR IS '회원 마스터 테이블';
COMMENT ON COLUMN TB_USER_MSTR.USER_ID IS '회원 일련번호(PK)';
COMMENT ON COLUMN TB_USER_MSTR.USER_EMAIL IS '회원 로그인 이메일';
COMMENT ON COLUMN TB_USER_MSTR.USER_PWD IS '암호화된 비밀번호';
COMMENT ON COLUMN TB_USER_MSTR.USER_NICKNM IS '회원 닉네임';
COMMENT ON COLUMN TB_USER_MSTR.SNS_PROVIDER IS '가입 출처(LOCAL, KAKAO, NAVER)';
COMMENT ON COLUMN TB_USER_MSTR.DEL_YN IS '탈퇴 여부(Y: 탈퇴, N: 유지)';