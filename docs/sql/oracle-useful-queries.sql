/*
 * RouteMate 운영 조회 쿼리 모음 (Oracle)
 * 기준 스키마: Flyway V1 ~ V19
 * 모든 쿼리는 SELECT 전용이며 서로 독립적으로 실행할 수 있습니다.
 * :keyword, :product_id 같은 값은 SQL Developer의 바인드 변수입니다.
 *
 * 현재 주문/예약/결제 테이블은 없으므로 실제 판매량, 매출, 전환율은 조회할 수 없습니다.
 */

--------------------------------------------------------------------------------
-- 01. 주요 테이블별 현재 데이터 건수
--------------------------------------------------------------------------------
SELECT '회원' AS DATA_NAME, COUNT(*) AS ROW_COUNT FROM TB_USER_MSTR
UNION ALL SELECT '국가', COUNT(*) FROM TB_COUNTRY
UNION ALL SELECT '지역', COUNT(*) FROM TB_REGION
UNION ALL SELECT '여행지', COUNT(*) FROM TB_DESTINATION
UNION ALL SELECT '여행상품', COUNT(*) FROM TB_TRAVEL_PRODUCT
UNION ALL SELECT '상품옵션', COUNT(*) FROM TB_TRAVEL_PRODUCT_OPTION
UNION ALL SELECT '여행일정', COUNT(*) FROM TB_TRAVEL_PLAN
UNION ALL SELECT '일정상세', COUNT(*) FROM TB_TRAVEL_SCHEDULE
UNION ALL SELECT '추천여행지', COUNT(*) FROM TB_DEST_RECOMMEND
ORDER BY DATA_NAME;


--------------------------------------------------------------------------------
-- 02. Flyway 적용 이력과 성공 여부
--------------------------------------------------------------------------------
SELECT INSTALLED_RANK,
       VERSION,
       DESCRIPTION,
       TYPE,
       SCRIPT,
       INSTALLED_ON,
       EXECUTION_TIME,
       SUCCESS
FROM FLYWAY_SCHEMA_HISTORY
ORDER BY INSTALLED_RANK DESC;


--------------------------------------------------------------------------------
-- 03. 옵션상품 관리용 전체 목록
-- 국가/지역/여행지, 최저~최고 옵션가, 활성 옵션 수를 한 번에 확인합니다.
--------------------------------------------------------------------------------
SELECT p.PRODUCT_ID,
       c.COUNTRY_NAME,
       r.REGION_NAME,
       d.DEST_NAME,
       p.PRODUCT_NAME,
       p.PRODUCT_TYPE,
       p.PROVIDER_NAME,
       p.PRICE AS BASE_PRICE,
       p.CURRENCY,
       COUNT(o.OPTION_ID) AS OPTION_COUNT,
       SUM(CASE WHEN o.USE_YN = 'Y' THEN 1 ELSE 0 END) AS ACTIVE_OPTION_COUNT,
       MIN(CASE WHEN o.USE_YN = 'Y' THEN o.PRICE END) AS MIN_OPTION_PRICE,
       MAX(CASE WHEN o.USE_YN = 'Y' THEN o.PRICE END) AS MAX_OPTION_PRICE,
       p.USE_YN,
       p.CREATE_DT
FROM TB_TRAVEL_PRODUCT p
JOIN TB_DESTINATION d ON d.DEST_ID = p.DEST_ID
JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
JOIN TB_COUNTRY c ON c.COUNTRY_ID = d.COUNTRY_ID
LEFT JOIN TB_TRAVEL_PRODUCT_OPTION o ON o.PRODUCT_ID = p.PRODUCT_ID
GROUP BY p.PRODUCT_ID, c.COUNTRY_NAME, r.REGION_NAME, d.DEST_NAME,
         p.PRODUCT_NAME, p.PRODUCT_TYPE, p.PROVIDER_NAME, p.PRICE,
         p.CURRENCY, p.USE_YN, p.CREATE_DT
ORDER BY p.CREATE_DT DESC, p.PRODUCT_ID DESC;


--------------------------------------------------------------------------------
-- 04. 상품 검색 및 필터
-- 예: VAR keyword VARCHAR2(100); EXEC :keyword := '야경';
-- 값이 필요 없는 조건은 NULL로 지정할 수 있습니다.
--------------------------------------------------------------------------------
SELECT p.PRODUCT_ID,
       c.COUNTRY_NAME,
       r.REGION_NAME,
       d.DEST_NAME,
       p.PRODUCT_NAME,
       p.PRODUCT_TYPE,
       p.PROVIDER_NAME,
       p.PRICE,
       p.CURRENCY,
       p.USE_YN
FROM TB_TRAVEL_PRODUCT p
JOIN TB_DESTINATION d ON d.DEST_ID = p.DEST_ID
JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
JOIN TB_COUNTRY c ON c.COUNTRY_ID = d.COUNTRY_ID
WHERE (:keyword IS NULL
       OR UPPER(p.PRODUCT_NAME) LIKE '%' || UPPER(:keyword) || '%'
       OR UPPER(d.DEST_NAME) LIKE '%' || UPPER(:keyword) || '%'
       OR UPPER(p.PROVIDER_NAME) LIKE '%' || UPPER(:keyword) || '%')
  AND (:country_code IS NULL OR c.COUNTRY_CODE = :country_code)
  AND (:product_type IS NULL OR p.PRODUCT_TYPE = :product_type)
  AND (:use_yn IS NULL OR p.USE_YN = :use_yn)
  AND (:min_price IS NULL OR p.PRICE >= :min_price)
  AND (:max_price IS NULL OR p.PRICE <= :max_price)
ORDER BY p.SORT_ORDER, p.PRODUCT_ID DESC;


--------------------------------------------------------------------------------
-- 05. 특정 상품의 상세 정보
--------------------------------------------------------------------------------
SELECT p.PRODUCT_ID,
       c.COUNTRY_NAME,
       r.REGION_NAME,
       d.DEST_NAME,
       p.PRODUCT_NAME,
       p.PRODUCT_SUMMARY,
       p.PRODUCT_TYPE,
       p.PROVIDER_NAME,
       p.PRODUCT_DESC,
       p.COURSE_TEXT,
       p.INCLUDED_TEXT,
       p.EXCLUDED_TEXT,
       p.USAGE_GUIDE_TEXT,
       p.NOTICE_TEXT,
       p.CANCELLATION_POLICY_TEXT,
       p.FAQ_TEXT,
       p.MEETING_TIME,
       p.MEETING_PLACE,
       p.PRICE,
       p.CURRENCY,
       p.IMAGE_URL,
       p.DETAIL_IMAGE_URL,
       p.BOOKING_URL,
       p.USE_YN,
       p.CREATE_DT
FROM TB_TRAVEL_PRODUCT p
JOIN TB_DESTINATION d ON d.DEST_ID = p.DEST_ID
JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
JOIN TB_COUNTRY c ON c.COUNTRY_ID = d.COUNTRY_ID
WHERE p.PRODUCT_ID = :product_id;


--------------------------------------------------------------------------------
-- 06. 특정 상품의 판매 옵션
--------------------------------------------------------------------------------
SELECT o.OPTION_ID,
       o.OPTION_NAME,
       o.OPTION_DESC,
       o.PRICE,
       o.CURRENCY,
       o.CONFIRMATION_TYPE,
       o.VALIDITY_TEXT,
       o.CANCELLATION_POLICY,
       o.USE_YN,
       o.SORT_ORDER
FROM TB_TRAVEL_PRODUCT_OPTION o
WHERE o.PRODUCT_ID = :product_id
ORDER BY o.SORT_ORDER, o.OPTION_ID;


--------------------------------------------------------------------------------
-- 07. 국가별 상품 공급 현황
-- 상품이 부족한 국가를 찾아 신규 상품 등록 우선순위를 정할 때 사용합니다.
--------------------------------------------------------------------------------
SELECT c.COUNTRY_CODE,
       c.COUNTRY_NAME,
       COUNT(DISTINCT r.REGION_ID) AS REGION_COUNT,
       COUNT(DISTINCT d.DEST_ID) AS DESTINATION_COUNT,
       COUNT(DISTINCT p.PRODUCT_ID) AS PRODUCT_COUNT,
       SUM(CASE WHEN p.USE_YN = 'Y' THEN 1 ELSE 0 END) AS ACTIVE_PRODUCT_COUNT,
       ROUND(AVG(CASE WHEN p.USE_YN = 'Y' THEN p.PRICE END), 0) AS AVG_ACTIVE_PRICE,
       MIN(CASE WHEN p.USE_YN = 'Y' THEN p.PRICE END) AS MIN_ACTIVE_PRICE,
       MAX(CASE WHEN p.USE_YN = 'Y' THEN p.PRICE END) AS MAX_ACTIVE_PRICE
FROM TB_COUNTRY c
LEFT JOIN TB_REGION r ON r.COUNTRY_ID = c.COUNTRY_ID
LEFT JOIN TB_DESTINATION d ON d.REGION_ID = r.REGION_ID
LEFT JOIN TB_TRAVEL_PRODUCT p ON p.DEST_ID = d.DEST_ID
GROUP BY c.COUNTRY_CODE, c.COUNTRY_NAME
ORDER BY PRODUCT_COUNT ASC, DESTINATION_COUNT DESC, c.COUNTRY_NAME;


--------------------------------------------------------------------------------
-- 08. 지역별 상품 공급 현황
--------------------------------------------------------------------------------
SELECT c.COUNTRY_NAME,
       r.REGION_CODE,
       r.REGION_NAME,
       COUNT(DISTINCT d.DEST_ID) AS DESTINATION_COUNT,
       COUNT(DISTINCT p.PRODUCT_ID) AS PRODUCT_COUNT,
       SUM(CASE WHEN p.USE_YN = 'Y' THEN 1 ELSE 0 END) AS ACTIVE_PRODUCT_COUNT
FROM TB_REGION r
JOIN TB_COUNTRY c ON c.COUNTRY_ID = r.COUNTRY_ID
LEFT JOIN TB_DESTINATION d ON d.REGION_ID = r.REGION_ID
LEFT JOIN TB_TRAVEL_PRODUCT p ON p.DEST_ID = d.DEST_ID
GROUP BY c.COUNTRY_NAME, r.REGION_CODE, r.REGION_NAME
ORDER BY PRODUCT_COUNT ASC, c.COUNTRY_NAME, r.SORT_ORDER;


--------------------------------------------------------------------------------
-- 09. 상품 유형별 가격 및 구성 통계
--------------------------------------------------------------------------------
SELECT p.PRODUCT_TYPE,
       COUNT(*) AS PRODUCT_COUNT,
       SUM(CASE WHEN p.USE_YN = 'Y' THEN 1 ELSE 0 END) AS ACTIVE_PRODUCT_COUNT,
       ROUND(AVG(p.PRICE), 0) AS AVG_PRICE,
       MIN(p.PRICE) AS MIN_PRICE,
       MAX(p.PRICE) AS MAX_PRICE,
       ROUND(AVG(NVL(option_summary.OPTION_COUNT, 0)), 1) AS AVG_OPTION_COUNT
FROM TB_TRAVEL_PRODUCT p
LEFT JOIN (
    SELECT PRODUCT_ID, COUNT(*) AS OPTION_COUNT
    FROM TB_TRAVEL_PRODUCT_OPTION
    GROUP BY PRODUCT_ID
) option_summary ON option_summary.PRODUCT_ID = p.PRODUCT_ID
GROUP BY p.PRODUCT_TYPE
ORDER BY PRODUCT_COUNT DESC, p.PRODUCT_TYPE;


--------------------------------------------------------------------------------
-- 10. 등록 상품이 없는 여행지
-- 좋아요가 높은데 상품이 없는 곳이 가장 먼저 상품화할 후보입니다.
--------------------------------------------------------------------------------
SELECT c.COUNTRY_NAME,
       r.REGION_NAME,
       d.DEST_ID,
       d.DEST_NAME,
       d.CATEGORY,
       d.LIKE_COUNT
FROM TB_DESTINATION d
JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
JOIN TB_COUNTRY c ON c.COUNTRY_ID = d.COUNTRY_ID
LEFT JOIN TB_TRAVEL_PRODUCT p ON p.DEST_ID = d.DEST_ID
WHERE p.PRODUCT_ID IS NULL
ORDER BY d.LIKE_COUNT DESC, c.COUNTRY_NAME, r.REGION_NAME;


--------------------------------------------------------------------------------
-- 11. 상품 상세정보 누락 검사
-- MISSING_FIELD_COUNT가 큰 상품부터 보완하면 됩니다.
--------------------------------------------------------------------------------
SELECT p.PRODUCT_ID,
       d.DEST_NAME,
       p.PRODUCT_NAME,
       p.USE_YN,
       (CASE WHEN p.PRODUCT_SUMMARY IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.PRODUCT_DESC IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.IMAGE_URL IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.DETAIL_IMAGE_URL IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.COURSE_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.INCLUDED_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.EXCLUDED_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.USAGE_GUIDE_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.NOTICE_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.CANCELLATION_POLICY_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.FAQ_TEXT IS NULL THEN 1 ELSE 0 END
        + CASE WHEN p.MEETING_PLACE IS NULL THEN 1 ELSE 0 END) AS MISSING_FIELD_COUNT,
       CASE WHEN p.PRODUCT_SUMMARY IS NULL THEN '요약, ' END
       || CASE WHEN p.PRODUCT_DESC IS NULL THEN '설명, ' END
       || CASE WHEN p.IMAGE_URL IS NULL THEN '대표이미지, ' END
       || CASE WHEN p.DETAIL_IMAGE_URL IS NULL THEN '상세이미지, ' END
       || CASE WHEN p.COURSE_TEXT IS NULL THEN '코스, ' END
       || CASE WHEN p.INCLUDED_TEXT IS NULL THEN '포함사항, ' END
       || CASE WHEN p.EXCLUDED_TEXT IS NULL THEN '불포함사항, ' END
       || CASE WHEN p.USAGE_GUIDE_TEXT IS NULL THEN '이용안내, ' END
       || CASE WHEN p.CANCELLATION_POLICY_TEXT IS NULL THEN '취소규정, ' END
       || CASE WHEN p.FAQ_TEXT IS NULL THEN 'FAQ, ' END AS MISSING_FIELDS
FROM TB_TRAVEL_PRODUCT p
JOIN TB_DESTINATION d ON d.DEST_ID = p.DEST_ID
WHERE p.PRODUCT_SUMMARY IS NULL
   OR p.PRODUCT_DESC IS NULL
   OR p.IMAGE_URL IS NULL
   OR p.DETAIL_IMAGE_URL IS NULL
   OR p.COURSE_TEXT IS NULL
   OR p.INCLUDED_TEXT IS NULL
   OR p.EXCLUDED_TEXT IS NULL
   OR p.USAGE_GUIDE_TEXT IS NULL
   OR p.NOTICE_TEXT IS NULL
   OR p.CANCELLATION_POLICY_TEXT IS NULL
   OR p.FAQ_TEXT IS NULL
   OR p.MEETING_PLACE IS NULL
ORDER BY MISSING_FIELD_COUNT DESC, p.PRODUCT_ID;


--------------------------------------------------------------------------------
-- 12. 활성 옵션이 없거나 옵션 가격이 비정상적인 상품
--------------------------------------------------------------------------------
SELECT p.PRODUCT_ID,
       p.PRODUCT_NAME,
       p.PRICE AS BASE_PRICE,
       COUNT(o.OPTION_ID) AS TOTAL_OPTION_COUNT,
       SUM(CASE WHEN o.USE_YN = 'Y' THEN 1 ELSE 0 END) AS ACTIVE_OPTION_COUNT,
       MIN(CASE WHEN o.USE_YN = 'Y' THEN o.PRICE END) AS MIN_ACTIVE_OPTION_PRICE,
       MAX(CASE WHEN o.USE_YN = 'Y' THEN o.PRICE END) AS MAX_ACTIVE_OPTION_PRICE
FROM TB_TRAVEL_PRODUCT p
LEFT JOIN TB_TRAVEL_PRODUCT_OPTION o ON o.PRODUCT_ID = p.PRODUCT_ID
GROUP BY p.PRODUCT_ID, p.PRODUCT_NAME, p.PRICE
HAVING SUM(CASE WHEN o.USE_YN = 'Y' THEN 1 ELSE 0 END) = 0
    OR MIN(CASE WHEN o.USE_YN = 'Y' THEN o.PRICE END) <= 0
ORDER BY p.PRODUCT_ID;


--------------------------------------------------------------------------------
-- 13. 중복 가능성이 있는 상품명
--------------------------------------------------------------------------------
SELECT p.DEST_ID,
       d.DEST_NAME,
       p.PRODUCT_NAME,
       COUNT(*) AS DUPLICATE_COUNT,
       LISTAGG(p.PRODUCT_ID, ', ') WITHIN GROUP (ORDER BY p.PRODUCT_ID) AS PRODUCT_IDS
FROM TB_TRAVEL_PRODUCT p
JOIN TB_DESTINATION d ON d.DEST_ID = p.DEST_ID
GROUP BY p.DEST_ID, d.DEST_NAME, p.PRODUCT_NAME
HAVING COUNT(*) > 1
ORDER BY DUPLICATE_COUNT DESC, d.DEST_NAME, p.PRODUCT_NAME;


--------------------------------------------------------------------------------
-- 14. 현재 홈페이지 노출 대상 추천 여행지
--------------------------------------------------------------------------------
SELECT rec.RECOMMEND_ID,
       c.COUNTRY_NAME,
       r.REGION_NAME,
       d.DEST_NAME,
       d.CATEGORY,
       NVL(rec.IMAGE_URL, d.IMAGE_URL) AS DISPLAY_IMAGE_URL,
       rec.DISPLAY_START_DT,
       rec.DISPLAY_END_DT,
       rec.SORT_ORDER
FROM TB_DEST_RECOMMEND rec
JOIN TB_DESTINATION d ON d.DEST_ID = rec.DEST_ID
JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
JOIN TB_COUNTRY c ON c.COUNTRY_ID = d.COUNTRY_ID
WHERE rec.USE_YN = 'Y'
  AND SYSTIMESTAMP BETWEEN rec.DISPLAY_START_DT AND rec.DISPLAY_END_DT
ORDER BY rec.SORT_ORDER, rec.RECOMMEND_ID;


--------------------------------------------------------------------------------
-- 15. 인기 여행지 TOP 30과 판매 상품 수
--------------------------------------------------------------------------------
SELECT *
FROM (
    SELECT c.COUNTRY_NAME,
           r.REGION_NAME,
           d.DEST_ID,
           d.DEST_NAME,
           d.CATEGORY,
           d.LIKE_COUNT,
           COUNT(p.PRODUCT_ID) AS PRODUCT_COUNT
    FROM TB_DESTINATION d
    JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
    JOIN TB_COUNTRY c ON c.COUNTRY_ID = d.COUNTRY_ID
    LEFT JOIN TB_TRAVEL_PRODUCT p
           ON p.DEST_ID = d.DEST_ID
          AND p.USE_YN = 'Y'
    GROUP BY c.COUNTRY_NAME, r.REGION_NAME, d.DEST_ID,
             d.DEST_NAME, d.CATEGORY, d.LIKE_COUNT
    ORDER BY d.LIKE_COUNT DESC, PRODUCT_COUNT DESC
)
WHERE ROWNUM <= 30;


--------------------------------------------------------------------------------
-- 16. 공개 여행일정 인기 순위
--------------------------------------------------------------------------------
SELECT *
FROM (
    SELECT p.PLAN_ID,
           p.USER_NICKNM,
           p.TITLE,
           p.TRAVEL_START_DT,
           p.TRAVEL_END_DT,
           p.SPOT_COUNT,
           p.LIKE_COUNT,
           p.VIEW_COUNT,
           ROUND(p.LIKE_COUNT / NULLIF(p.VIEW_COUNT, 0) * 100, 2) AS LIKE_RATE_PERCENT,
           p.MDFY_DT
    FROM TB_TRAVEL_PLAN p
    WHERE p.IS_PUBLIC = 'Y'
    ORDER BY p.LIKE_COUNT DESC, p.VIEW_COUNT DESC, p.MDFY_DT DESC
)
WHERE ROWNUM <= 30;


--------------------------------------------------------------------------------
-- 17. 특정 여행일정 전체 동선
--------------------------------------------------------------------------------
SELECT p.PLAN_ID,
       p.TITLE AS PLAN_TITLE,
       td.DAY_NO,
       td.PLAN_DATE,
       c.COUNTRY_NAME,
       r.REGION_NAME,
       dr.SORT_ORDER AS REGION_ORDER,
       s.SCHEDULE_TIME,
       s.SORT_ORDER AS SCHEDULE_ORDER,
       s.TITLE AS SCHEDULE_TITLE,
       s.LOCATION,
       s.MEMO,
       t.TRANSPORT_TYPE,
       t.TRANSPORT_NAME,
       t.DEPARTURE_TIME,
       t.ARRIVAL_TIME
FROM TB_TRAVEL_PLAN p
JOIN TB_TRAVEL_DAY td ON td.PLAN_ID = p.PLAN_ID
LEFT JOIN TB_TRAVEL_DAY_REGION dr ON dr.DAY_ID = td.DAY_ID
LEFT JOIN TB_COUNTRY c ON c.COUNTRY_ID = dr.COUNTRY_ID
LEFT JOIN TB_REGION r ON r.REGION_ID = dr.REGION_ID
LEFT JOIN TB_TRAVEL_SCHEDULE s ON s.DAY_REGION_ID = dr.DAY_REGION_ID
LEFT JOIN TB_TRAVEL_TRANSPORT t ON t.SCHEDULE_ID = s.SCHEDULE_ID
WHERE p.PLAN_ID = :plan_id
ORDER BY td.DAY_NO, dr.SORT_ORDER, s.SORT_ORDER;


--------------------------------------------------------------------------------
-- 18. 사용자가 일정에 가장 많이 넣은 국가/지역
-- 실제 일정 수요가 높은 곳에 신규 상품을 공급할 때 참고할 수 있습니다.
--------------------------------------------------------------------------------
SELECT c.COUNTRY_NAME,
       r.REGION_NAME,
       COUNT(DISTINCT p.PLAN_ID) AS PLAN_COUNT,
       COUNT(DISTINCT dr.DAY_REGION_ID) AS VISIT_DAY_COUNT,
       COUNT(DISTINCT s.SCHEDULE_ID) AS SCHEDULE_COUNT,
       COUNT(DISTINCT product.PRODUCT_ID) AS AVAILABLE_PRODUCT_COUNT
FROM TB_TRAVEL_DAY_REGION dr
JOIN TB_TRAVEL_DAY td ON td.DAY_ID = dr.DAY_ID
JOIN TB_TRAVEL_PLAN p ON p.PLAN_ID = td.PLAN_ID
JOIN TB_COUNTRY c ON c.COUNTRY_ID = dr.COUNTRY_ID
JOIN TB_REGION r ON r.REGION_ID = dr.REGION_ID
LEFT JOIN TB_TRAVEL_SCHEDULE s ON s.DAY_REGION_ID = dr.DAY_REGION_ID
LEFT JOIN TB_DESTINATION d ON d.REGION_ID = r.REGION_ID
LEFT JOIN TB_TRAVEL_PRODUCT product
       ON product.DEST_ID = d.DEST_ID
      AND product.USE_YN = 'Y'
GROUP BY c.COUNTRY_NAME, r.REGION_NAME
ORDER BY PLAN_COUNT DESC, SCHEDULE_COUNT DESC;


--------------------------------------------------------------------------------
-- 19. 최근 12개월 월별 회원가입 추이
--------------------------------------------------------------------------------
SELECT TO_CHAR(TRUNC(JOIN_DT, 'MM'), 'YYYY-MM') AS JOIN_MONTH,
       COUNT(*) AS JOIN_COUNT,
       SUM(CASE WHEN SNS_PROVIDER = 'LOCAL' THEN 1 ELSE 0 END) AS LOCAL_COUNT,
       SUM(CASE WHEN SNS_PROVIDER <> 'LOCAL' THEN 1 ELSE 0 END) AS SNS_COUNT
FROM TB_USER_MSTR
WHERE JOIN_DT >= ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -11)
  AND DEL_YN = 'N'
GROUP BY TRUNC(JOIN_DT, 'MM')
ORDER BY TRUNC(JOIN_DT, 'MM');


--------------------------------------------------------------------------------
-- 20. 회원 상태 및 가입 방식 통계
--------------------------------------------------------------------------------
SELECT USER_STAT_CD,
       SNS_PROVIDER,
       USER_ROLE,
       DEL_YN,
       COUNT(*) AS USER_COUNT
FROM TB_USER_MSTR
GROUP BY USER_STAT_CD, SNS_PROVIDER, USER_ROLE, DEL_YN
ORDER BY USER_STAT_CD, SNS_PROVIDER, USER_ROLE, DEL_YN;


--------------------------------------------------------------------------------
-- 21. 관리자 계정과 부여된 역할
--------------------------------------------------------------------------------
SELECT u.USER_ID,
       u.USER_EMAIL,
       u.USER_NICKNM,
       u.USER_STAT_CD,
       r.ROLE_CODE,
       r.ROLE_NAME,
       r.ROLE_LEVEL,
       d.DEPT_NAME,
       ur.PRIMARY_YN,
       r.USE_YN AS ROLE_USE_YN
FROM TB_ADMIN_USER_ROLE ur
JOIN TB_USER_MSTR u ON u.USER_ID = ur.USER_ID
JOIN TB_ADMIN_ROLE r ON r.ROLE_ID = ur.ROLE_ID
LEFT JOIN TB_ADMIN_DEPT d ON d.DEPT_ID = r.DEPT_ID
ORDER BY r.ROLE_LEVEL DESC, u.USER_EMAIL;


--------------------------------------------------------------------------------
-- 22. 역할별 접근 가능 메뉴와 권한
--------------------------------------------------------------------------------
SELECT r.ROLE_CODE,
       r.ROLE_NAME,
       m.MENU_CODE,
       m.MENU_NAME,
       m.MENU_PATH,
       p.PERMISSION_CODE,
       p.PERMISSION_NAME,
       rm.ALLOW_YN
FROM TB_ADMIN_ROLE_MENU rm
JOIN TB_ADMIN_ROLE r ON r.ROLE_ID = rm.ROLE_ID
JOIN TB_ADMIN_MENU m ON m.MENU_ID = rm.MENU_ID
LEFT JOIN TB_ADMIN_PERMISSION p ON p.PERMISSION_ID = rm.PERMISSION_ID
WHERE r.USE_YN = 'Y'
  AND m.USE_YN = 'Y'
ORDER BY r.ROLE_LEVEL DESC, m.SORT_ORDER, p.PERMISSION_CODE;


--------------------------------------------------------------------------------
-- 23. 여행지의 국가와 지역이 서로 불일치하는 데이터 검사
-- 정상이라면 결과가 0건이어야 합니다.
--------------------------------------------------------------------------------
SELECT d.DEST_ID,
       d.DEST_NAME,
       d.COUNTRY_ID AS DEST_COUNTRY_ID,
       r.COUNTRY_ID AS REGION_COUNTRY_ID,
       r.REGION_ID,
       r.REGION_NAME
FROM TB_DESTINATION d
JOIN TB_REGION r ON r.REGION_ID = d.REGION_ID
WHERE d.COUNTRY_ID <> r.COUNTRY_ID;


--------------------------------------------------------------------------------
-- 24. 상품/옵션 데이터 품질 종합 점검
-- 정상이라면 각 ERROR_COUNT가 모두 0이어야 합니다.
--------------------------------------------------------------------------------
SELECT '활성 상품이지만 활성 옵션 없음' AS CHECK_NAME, COUNT(*) AS ERROR_COUNT
FROM TB_TRAVEL_PRODUCT p
WHERE p.USE_YN = 'Y'
  AND NOT EXISTS (
      SELECT 1
      FROM TB_TRAVEL_PRODUCT_OPTION o
      WHERE o.PRODUCT_ID = p.PRODUCT_ID
        AND o.USE_YN = 'Y'
  )
UNION ALL
SELECT '상품 기본가격 음수', COUNT(*)
FROM TB_TRAVEL_PRODUCT
WHERE PRICE < 0
UNION ALL
SELECT '옵션 가격 음수', COUNT(*)
FROM TB_TRAVEL_PRODUCT_OPTION
WHERE PRICE < 0
UNION ALL
SELECT '통화 불일치', COUNT(*)
FROM TB_TRAVEL_PRODUCT_OPTION o
JOIN TB_TRAVEL_PRODUCT p ON p.PRODUCT_ID = o.PRODUCT_ID
WHERE o.CURRENCY <> p.CURRENCY
UNION ALL
SELECT '유효하지 않은 확정방식', COUNT(*)
FROM TB_TRAVEL_PRODUCT_OPTION
WHERE CONFIRMATION_TYPE NOT IN ('INSTANT', 'MANUAL');
