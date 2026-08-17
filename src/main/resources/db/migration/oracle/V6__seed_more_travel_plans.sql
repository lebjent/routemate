DECLARE
    v_plan_id NUMBER;
    v_day_id NUMBER;
    v_day_region_id NUMBER;
    v_country_id NUMBER;
    v_region_id NUMBER;

    PROCEDURE add_plan(p_title VARCHAR2, p_country VARCHAR2, p_region VARCHAR2, p_date DATE, p_time VARCHAR2, p_spot VARCHAR2) IS
    BEGIN
        INSERT INTO TB_TRAVEL_PLAN (USER_NICKNM, TITLE, DESCRIPTION, TRAVEL_START_DT, TRAVEL_END_DT, SPOT_COUNT, LIKE_COUNT, IS_PUBLIC)
        VALUES ('RouteMate', p_title, 'RouteMate 샘플 여행 일정입니다.', p_date, p_date + 2, 1, 0, 'Y')
        RETURNING PLAN_ID INTO v_plan_id;
        INSERT INTO TB_TRAVEL_DAY (PLAN_ID, DAY_NO, PLAN_DATE) VALUES (v_plan_id, 1, p_date)
        RETURNING DAY_ID INTO v_day_id;
        SELECT c.COUNTRY_ID, r.REGION_ID INTO v_country_id, v_region_id
        FROM TB_COUNTRY c JOIN TB_REGION r ON r.COUNTRY_ID = c.COUNTRY_ID
        WHERE c.COUNTRY_CODE = p_country AND r.REGION_CODE = p_region;
        INSERT INTO TB_TRAVEL_DAY_REGION (DAY_ID, COUNTRY_ID, REGION_ID, REGION_NOTE, SORT_ORDER)
        VALUES (v_day_id, v_country_id, v_region_id, '샘플 여행지', 1)
        RETURNING DAY_REGION_ID INTO v_day_region_id;
        INSERT INTO TB_TRAVEL_SCHEDULE (DAY_REGION_ID, SCHEDULE_TIME, TITLE, LOCATION, MEMO, SORT_ORDER)
        VALUES (v_day_region_id, p_time, p_spot, p_spot, '추천 일정 샘플', 1);
    END;
BEGIN
    add_plan('서울 도심 문화 여행', 'KR', 'SEL', DATE '2027-01-10', '09:00', '경복궁과 북촌 한옥마을');
    add_plan('오사카 미식 여행', 'JP', 'OSA', DATE '2027-02-05', '10:00', '도톤보리 맛집 탐방');
    add_plan('방콕 주말 여행', 'TH', 'BKK', DATE '2027-03-12', '11:00', '왕궁과 짜오프라야강');
    add_plan('싱가포르 야경 여행', 'SG', 'SIN', DATE '2027-04-02', '16:00', '마리나 베이 샌즈');
    add_plan('타이베이 먹거리 여행', 'TW', 'TPE', DATE '2027-05-08', '09:30', '타이베이 101과 야시장');
    add_plan('상하이 도시 여행', 'CN', 'SHA', DATE '2027-06-11', '10:00', '와이탄 야경 산책');
    add_plan('시드니 자연 여행', 'AU', 'SYD', DATE '2027-07-16', '08:30', '오페라하우스와 본다이비치');
    add_plan('로마 역사 여행', 'IT', 'ROM', DATE '2027-08-06', '09:00', '콜로세움 투어');
    add_plan('바르셀로나 건축 여행', 'ES', 'BCN', DATE '2027-09-03', '10:30', '사그라다 파밀리아');
    add_plan('베를린 예술 여행', 'DE', 'BER', DATE '2027-10-15', '11:00', '박물관 섬 관람');
END;
/
