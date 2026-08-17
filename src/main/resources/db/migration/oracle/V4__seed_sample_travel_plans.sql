DECLARE
    v_plan_id NUMBER;
    v_day_id NUMBER;
    v_day_region_id NUMBER;
    v_country_id NUMBER;
    v_region_id NUMBER;

    PROCEDURE add_day(
        p_plan_id NUMBER, p_day_no NUMBER, p_plan_date DATE,
        p_country_code VARCHAR2, p_region_code VARCHAR2, p_note VARCHAR2,
        p_time VARCHAR2, p_title VARCHAR2, p_location VARCHAR2, p_memo VARCHAR2
    ) IS
    BEGIN
        INSERT INTO TB_TRAVEL_DAY (PLAN_ID, DAY_NO, PLAN_DATE)
        VALUES (p_plan_id, p_day_no, p_plan_date)
        RETURNING DAY_ID INTO v_day_id;

        SELECT c.COUNTRY_ID, r.REGION_ID INTO v_country_id, v_region_id
        FROM TB_COUNTRY c JOIN TB_REGION r ON r.COUNTRY_ID = c.COUNTRY_ID
        WHERE c.COUNTRY_CODE = p_country_code AND r.REGION_CODE = p_region_code;

        INSERT INTO TB_TRAVEL_DAY_REGION (DAY_ID, COUNTRY_ID, REGION_ID, REGION_NOTE, SORT_ORDER)
        VALUES (v_day_id, v_country_id, v_region_id, p_note, 1)
        RETURNING DAY_REGION_ID INTO v_day_region_id;

        INSERT INTO TB_TRAVEL_SCHEDULE (DAY_REGION_ID, SCHEDULE_TIME, TITLE, LOCATION, MEMO, SORT_ORDER)
        VALUES (v_day_region_id, p_time, p_title, p_location, p_memo, 1);
    END;
BEGIN
    INSERT INTO TB_TRAVEL_PLAN (USER_NICKNM, TITLE, DESCRIPTION, TRAVEL_START_DT, TRAVEL_END_DT, SPOT_COUNT, LIKE_COUNT, IS_PUBLIC)
    VALUES ('RouteMate', '일본 도쿄·교토 2박 3일', '도쿄의 도심과 교토의 전통 풍경을 함께 둘러보는 샘플 일정', DATE '2026-09-10', DATE '2026-09-12', 2, 0, 'Y')
    RETURNING PLAN_ID INTO v_plan_id;
    add_day(v_plan_id, 1, DATE '2026-09-10', 'JP', 'TYO', '도쿄역 근처 숙소', '09:00', '도쿄 시내 산책', '아사쿠사·도쿄 스카이트리', '센소지와 스미다강을 둘러봅니다.');
    add_day(v_plan_id, 2, DATE '2026-09-11', 'JP', 'KYO', '신칸센 이동', '10:00', '교토 사찰 투어', '후시미이나리·기요미즈데라', '아침 일찍 출발합니다.');

    INSERT INTO TB_TRAVEL_PLAN (USER_NICKNM, TITLE, DESCRIPTION, TRAVEL_START_DT, TRAVEL_END_DT, SPOT_COUNT, LIKE_COUNT, IS_PUBLIC)
    VALUES ('RouteMate', '파리·런던 문화 여행', '유럽 대표 도시의 미술관과 랜드마크를 둘러보는 샘플 일정', DATE '2026-10-03', DATE '2026-10-05', 2, 0, 'Y')
    RETURNING PLAN_ID INTO v_plan_id;
    add_day(v_plan_id, 1, DATE '2026-10-03', 'FR', 'PAR', '파리 중심가 숙소', '09:30', '파리 미술관 관람', '루브르 박물관', '예약 시간을 확인합니다.');
    add_day(v_plan_id, 2, DATE '2026-10-04', 'GB', 'LON', '런던 지하철 이용', '10:00', '런던 랜드마크 투어', '빅벤·타워 브리지', '오이스터 카드 또는 컨택리스 결제.');

    INSERT INTO TB_TRAVEL_PLAN (USER_NICKNM, TITLE, DESCRIPTION, TRAVEL_START_DT, TRAVEL_END_DT, SPOT_COUNT, LIKE_COUNT, IS_PUBLIC)
    VALUES ('RouteMate', '베트남 푸꾸옥 휴양 일정', '푸꾸옥 해변과 야시장 중심의 여유로운 샘플 일정', DATE '2026-11-01', DATE '2026-11-03', 2, 0, 'Y')
    RETURNING PLAN_ID INTO v_plan_id;
    add_day(v_plan_id, 1, DATE '2026-11-01', 'VN', 'PQC', '해변 리조트 체크인', '14:00', '리조트 체크인과 해변 휴식', '롱비치', '공항 픽업을 미리 예약합니다.');
    add_day(v_plan_id, 2, DATE '2026-11-02', 'VN', 'PQC', '선셋 시간 확인', '17:00', '푸꾸옥 야시장 방문', '즈엉동 야시장', '현지 음식과 기념품을 즐깁니다.');
END;
/
