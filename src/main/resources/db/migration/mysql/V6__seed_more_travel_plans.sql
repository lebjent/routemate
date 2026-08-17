INSERT INTO TB_TRAVEL_PLAN (USER_NICKNM, TITLE, DESCRIPTION, TRAVEL_START_DT, TRAVEL_END_DT, SPOT_COUNT, LIKE_COUNT, IS_PUBLIC)
VALUES ('RouteMate', '서울 도심 문화 여행', 'RouteMate 샘플 여행 일정입니다.', '2027-01-10', '2027-01-12', 1, 0, 'Y');
SET @plan_id = LAST_INSERT_ID();
INSERT INTO TB_TRAVEL_DAY (PLAN_ID, DAY_NO, PLAN_DATE) VALUES (@plan_id, 1, '2027-01-10'); SET @day_id = LAST_INSERT_ID();
INSERT INTO TB_TRAVEL_DAY_REGION (DAY_ID, COUNTRY_ID, REGION_ID, REGION_NOTE, SORT_ORDER) SELECT @day_id, c.COUNTRY_ID, r.REGION_ID, '샘플 여행지', 1 FROM TB_COUNTRY c JOIN TB_REGION r ON r.COUNTRY_ID=c.COUNTRY_ID WHERE c.COUNTRY_CODE='KR' AND r.REGION_CODE='SEL'; SET @day_region_id = LAST_INSERT_ID();
INSERT INTO TB_TRAVEL_SCHEDULE (DAY_REGION_ID, SCHEDULE_TIME, TITLE, LOCATION, MEMO, SORT_ORDER) VALUES (@day_region_id, '09:00', '경복궁과 북촌 한옥마을', '경복궁과 북촌 한옥마을', '추천 일정 샘플', 1);

INSERT INTO TB_TRAVEL_PLAN (USER_NICKNM, TITLE, DESCRIPTION, TRAVEL_START_DT, TRAVEL_END_DT, SPOT_COUNT, LIKE_COUNT, IS_PUBLIC)
SELECT 'RouteMate', x.title, 'RouteMate 샘플 여행 일정입니다.', x.start_date, DATE_ADD(x.start_date, INTERVAL 2 DAY), 1, 0, 'Y'
FROM (SELECT '오사카 미식 여행' title, '2027-02-05' start_date, 'JP' country_code, 'OSA' region_code, '10:00' schedule_time, '도톤보리 맛집 탐방' spot UNION ALL
SELECT '방콕 주말 여행','2027-03-12','TH','BKK','11:00','왕궁과 짜오프라야강' UNION ALL SELECT '싱가포르 야경 여행','2027-04-02','SG','SIN','16:00','마리나 베이 샌즈' UNION ALL SELECT '타이베이 먹거리 여행','2027-05-08','TW','TPE','09:30','타이베이 101과 야시장' UNION ALL SELECT '상하이 도시 여행','2027-06-11','CN','SHA','10:00','와이탄 야경 산책' UNION ALL SELECT '시드니 자연 여행','2027-07-16','AU','SYD','08:30','오페라하우스와 본다이비치' UNION ALL SELECT '로마 역사 여행','2027-08-06','IT','ROM','09:00','콜로세움 투어' UNION ALL SELECT '바르셀로나 건축 여행','2027-09-03','ES','BCN','10:30','사그라다 파밀리아' UNION ALL SELECT '베를린 예술 여행','2027-10-15','DE','BER','11:00','박물관 섬 관람') x;
