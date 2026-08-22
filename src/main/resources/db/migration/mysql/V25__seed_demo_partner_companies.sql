INSERT INTO TB_PARTNER_COMPANY (PARTNER_CODE, PARTNER_NAME, BUSINESS_NUMBER, REPRESENTATIVE_NAME, MANAGER_NAME, MANAGER_EMAIL, MANAGER_PHONE, WEBSITE_URL, COMMISSION_RATE, CONTRACT_START_DATE, CONTRACT_END_DATE, PARTNER_STATUS, MEMO)
VALUES
('DEMO-01', '하늘길투어', '101-86-10001', '김하늘', '이여행', 'contact01@demo.routemate.kr', '02-1000-1001', 'https://demo.routemate.kr/skyway', 12.00, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-02', '도시탐험컴퍼니', '102-86-10002', '박도시', '최탐험', 'contact02@demo.routemate.kr', '02-1000-1002', 'https://demo.routemate.kr/city', 13.50, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-03', '오션모빌리티', '103-86-10003', '서바다', '윤이동', 'contact03@demo.routemate.kr', '02-1000-1003', 'https://demo.routemate.kr/ocean', 10.00, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-04', '로컬프렌즈', '104-86-10004', '한동네', '장가이드', 'contact04@demo.routemate.kr', '02-1000-1004', 'https://demo.routemate.kr/local', 15.00, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-05', '셀렉트트래블', '105-86-10005', '문선택', '오예약', 'contact05@demo.routemate.kr', '02-1000-1005', 'https://demo.routemate.kr/select', 11.00, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-06', '원더데이익스피리언스', '106-86-10006', '강원더', '류체험', 'contact06@demo.routemate.kr', '02-1000-1006', 'https://demo.routemate.kr/wonder', 14.00, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-07', '글로벌패스코리아', '107-86-10007', '조글로벌', '신패스', 'contact07@demo.routemate.kr', '02-1000-1007', 'https://demo.routemate.kr/globalpass', 9.50, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-08', '트래블픽', '108-86-10008', '임트립', '배픽', 'contact08@demo.routemate.kr', '02-1000-1008', 'https://demo.routemate.kr/travelpick', 12.50, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-09', '조이풀액티비티', '109-86-10009', '송조이', '남액티비티', 'contact09@demo.routemate.kr', '02-1000-1009', 'https://demo.routemate.kr/joyful', 16.00, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'),
('DEMO-10', '스마트트립솔루션', '110-86-10010', '유스마트', '권솔루션', 'contact10@demo.routemate.kr', '02-1000-1010', 'https://demo.routemate.kr/smarttrip', 11.50, '2026-01-01', '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사')
ON DUPLICATE KEY UPDATE PARTNER_NAME = VALUES(PARTNER_NAME), BUSINESS_NUMBER = VALUES(BUSINESS_NUMBER), REPRESENTATIVE_NAME = VALUES(REPRESENTATIVE_NAME), MANAGER_NAME = VALUES(MANAGER_NAME), MANAGER_EMAIL = VALUES(MANAGER_EMAIL), MANAGER_PHONE = VALUES(MANAGER_PHONE), WEBSITE_URL = VALUES(WEBSITE_URL), COMMISSION_RATE = VALUES(COMMISSION_RATE), CONTRACT_START_DATE = VALUES(CONTRACT_START_DATE), CONTRACT_END_DATE = VALUES(CONTRACT_END_DATE), PARTNER_STATUS = VALUES(PARTNER_STATUS), MEMO = VALUES(MEMO);

UPDATE TB_TRAVEL_PRODUCT product
JOIN TB_PARTNER_COMPANY partner ON partner.PARTNER_CODE = CONCAT('DEMO-', LPAD(MOD(product.PRODUCT_ID - 1, 10) + 1, 2, '0'))
SET product.PARTNER_ID = partner.PARTNER_ID,
    product.PROVIDER_NAME = partner.PARTNER_NAME;

DELETE legacy FROM TB_PARTNER_COMPANY legacy
LEFT JOIN TB_PARTNER_USER partner_user ON partner_user.PARTNER_ID = legacy.PARTNER_ID
WHERE legacy.PARTNER_CODE LIKE 'LEGACY-%' AND partner_user.PARTNER_USER_ID IS NULL;
