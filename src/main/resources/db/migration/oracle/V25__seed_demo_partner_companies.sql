MERGE INTO TB_PARTNER_COMPANY target
USING (
    SELECT 'DEMO-01' partner_code, '하늘길투어' partner_name, '101-86-10001' business_number, '김하늘' representative_name, '이여행' manager_name, 'contact01@demo.routemate.kr' manager_email, '02-1000-1001' manager_phone, 'https://demo.routemate.kr/skyway' website_url, 12.00 commission_rate FROM DUAL
    UNION ALL SELECT 'DEMO-02', '도시탐험컴퍼니', '102-86-10002', '박도시', '최탐험', 'contact02@demo.routemate.kr', '02-1000-1002', 'https://demo.routemate.kr/city', 13.50 FROM DUAL
    UNION ALL SELECT 'DEMO-03', '오션모빌리티', '103-86-10003', '서바다', '윤이동', 'contact03@demo.routemate.kr', '02-1000-1003', 'https://demo.routemate.kr/ocean', 10.00 FROM DUAL
    UNION ALL SELECT 'DEMO-04', '로컬프렌즈', '104-86-10004', '한동네', '장가이드', 'contact04@demo.routemate.kr', '02-1000-1004', 'https://demo.routemate.kr/local', 15.00 FROM DUAL
    UNION ALL SELECT 'DEMO-05', '셀렉트트래블', '105-86-10005', '문선택', '오예약', 'contact05@demo.routemate.kr', '02-1000-1005', 'https://demo.routemate.kr/select', 11.00 FROM DUAL
    UNION ALL SELECT 'DEMO-06', '원더데이익스피리언스', '106-86-10006', '강원더', '류체험', 'contact06@demo.routemate.kr', '02-1000-1006', 'https://demo.routemate.kr/wonder', 14.00 FROM DUAL
    UNION ALL SELECT 'DEMO-07', '글로벌패스코리아', '107-86-10007', '조글로벌', '신패스', 'contact07@demo.routemate.kr', '02-1000-1007', 'https://demo.routemate.kr/globalpass', 9.50 FROM DUAL
    UNION ALL SELECT 'DEMO-08', '트래블픽', '108-86-10008', '임트립', '배픽', 'contact08@demo.routemate.kr', '02-1000-1008', 'https://demo.routemate.kr/travelpick', 12.50 FROM DUAL
    UNION ALL SELECT 'DEMO-09', '조이풀액티비티', '109-86-10009', '송조이', '남액티비티', 'contact09@demo.routemate.kr', '02-1000-1009', 'https://demo.routemate.kr/joyful', 16.00 FROM DUAL
    UNION ALL SELECT 'DEMO-10', '스마트트립솔루션', '110-86-10010', '유스마트', '권솔루션', 'contact10@demo.routemate.kr', '02-1000-1010', 'https://demo.routemate.kr/smarttrip', 11.50 FROM DUAL
) source
ON (target.PARTNER_CODE = source.partner_code)
WHEN MATCHED THEN UPDATE SET
    target.PARTNER_NAME = source.partner_name,
    target.BUSINESS_NUMBER = source.business_number,
    target.REPRESENTATIVE_NAME = source.representative_name,
    target.MANAGER_NAME = source.manager_name,
    target.MANAGER_EMAIL = source.manager_email,
    target.MANAGER_PHONE = source.manager_phone,
    target.WEBSITE_URL = source.website_url,
    target.COMMISSION_RATE = source.commission_rate,
    target.CONTRACT_START_DATE = DATE '2026-01-01',
    target.CONTRACT_END_DATE = DATE '2026-12-31',
    target.PARTNER_STATUS = 'ACTIVE',
    target.MEMO = '개발 환경용 더미 파트너사',
    target.MDFY_DT = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    PARTNER_CODE, PARTNER_NAME, BUSINESS_NUMBER, REPRESENTATIVE_NAME, MANAGER_NAME, MANAGER_EMAIL,
    MANAGER_PHONE, WEBSITE_URL, COMMISSION_RATE, CONTRACT_START_DATE, CONTRACT_END_DATE, PARTNER_STATUS, MEMO
) VALUES (
    source.partner_code, source.partner_name, source.business_number, source.representative_name, source.manager_name, source.manager_email,
    source.manager_phone, source.website_url, source.commission_rate, DATE '2026-01-01', DATE '2026-12-31', 'ACTIVE', '개발 환경용 더미 파트너사'
);

MERGE INTO TB_TRAVEL_PRODUCT product
USING (
    SELECT p.PRODUCT_ID product_id, partner.PARTNER_ID partner_id, partner.PARTNER_NAME partner_name
    FROM TB_TRAVEL_PRODUCT p
    JOIN TB_PARTNER_COMPANY partner
      ON partner.PARTNER_CODE = 'DEMO-' || LPAD(MOD(p.PRODUCT_ID - 1, 10) + 1, 2, '0')
) source
ON (product.PRODUCT_ID = source.product_id)
WHEN MATCHED THEN UPDATE SET
    product.PARTNER_ID = source.partner_id,
    product.PROVIDER_NAME = source.partner_name;

DELETE FROM TB_PARTNER_COMPANY legacy
WHERE legacy.PARTNER_CODE LIKE 'LEGACY-%'
  AND NOT EXISTS (SELECT 1 FROM TB_PARTNER_USER partner_user WHERE partner_user.PARTNER_ID = legacy.PARTNER_ID);
