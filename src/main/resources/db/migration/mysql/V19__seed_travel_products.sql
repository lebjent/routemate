-- 100 destinations x 1 richly configured product, with three purchasable options each.
INSERT INTO TB_TRAVEL_PRODUCT (
    DEST_ID, PRODUCT_NAME, PRODUCT_SUMMARY, PRODUCT_TYPE, PROVIDER_NAME,
    PRODUCT_DESC, IMAGE_URL, DETAIL_IMAGE_URL, COURSE_TEXT, INCLUDED_TEXT,
    EXCLUDED_TEXT, USAGE_GUIDE_TEXT, NOTICE_TEXT, CANCELLATION_POLICY_TEXT,
    FAQ_TEXT, MEETING_TIME, MEETING_PLACE, BOOKING_URL,
    PRICE, CURRENCY, USE_YN, SORT_ORDER
)
SELECT
    d.DEST_ID,
    CONCAT(d.DEST_NAME, ' ', CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN '핵심 명소 패스트트랙 입장권'
        WHEN 1 THEN '현지 가이드 반일 하이라이트 투어'
        WHEN 2 THEN '선셋 & 야경 소그룹 투어'
        WHEN 3 THEN '로컬 미식 골목 투어'
        WHEN 4 THEN '공항 픽업 프라이빗 차량'
        WHEN 5 THEN '여행용 eSIM 데이터 패키지'
        WHEN 6 THEN '감성 스냅 촬영 체험'
        WHEN 7 THEN '가족 맞춤 원데이 투어'
        WHEN 8 THEN '대중교통 자유이용 패스'
        ELSE '프리미엄 프라이빗 투어' END),
    CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN '줄 서는 시간을 줄이고 모바일 QR로 대표 명소에 바로 입장하세요.'
        WHEN 1 THEN '한국어 안내와 함께 꼭 봐야 할 장소를 효율적으로 둘러보는 반일 코스입니다.'
        WHEN 2 THEN '해 질 무렵 출발해 낮과 밤의 서로 다른 풍경을 한 번에 만납니다.'
        WHEN 3 THEN '현지인이 사랑하는 맛집과 시장 음식을 소규모로 경험합니다.'
        WHEN 4 THEN '비행 일정에 맞춰 기사님이 대기하는 편안한 단독 이동 서비스입니다.'
        WHEN 5 THEN '수령과 반납 없이 QR 설치만으로 바로 사용하는 여행 데이터입니다.'
        WHEN 6 THEN '여행의 자연스러운 순간을 현지 전문 포토그래퍼가 기록합니다.'
        WHEN 7 THEN '아이와 부모 모두 편하도록 이동 속도와 방문지를 조정하는 가족 코스입니다.'
        WHEN 8 THEN '정해진 기간 동안 주요 교통수단을 자유롭게 이용하는 실속 패스입니다.'
        ELSE '전용 차량과 전담 가이드로 일정과 동선을 자유롭게 설계합니다.' END,
    CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN 'TICKET' WHEN 4 THEN 'TRANSFER' WHEN 5 THEN 'SIM'
        WHEN 8 THEN 'TRANSFER' WHEN 3 THEN 'ETC' WHEN 6 THEN 'ETC' ELSE 'TOUR' END,
    CASE MOD(d.RN - 1, 6)
        WHEN 0 THEN 'RouteMate Select' WHEN 1 THEN 'Local Friends'
        WHEN 2 THEN 'Blue Horizon Travel' WHEN 3 THEN 'City Expert'
        WHEN 4 THEN 'Easy Trip Mobility' ELSE 'Wonder Day Experiences' END,
    CONCAT('이 상품은 ', d.DEST_NAME, ' 여행에서 자주 생기는 예약과 이동의 불편을 줄이도록 구성했습니다. ',
           '처음 방문하는 여행자도 이해하기 쉬운 안내, 검증된 운영 동선, 명확한 포함 사항을 제공합니다. ',
           '예약 후 발송되는 바우처에서 최종 집합 정보와 현지 연락처를 확인할 수 있습니다.'),
    CASE MOD(d.RN - 1, 5)
        WHEN 0 THEN 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=80'
        WHEN 1 THEN 'https://images.unsplash.com/photo-1526772662000-3f88f10405ff?auto=format&fit=crop&w=1400&q=80'
        WHEN 2 THEN 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=1400&q=80'
        WHEN 3 THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=80'
        ELSE 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1400&q=80' END,
    CASE MOD(d.RN - 1, 4)
        WHEN 0 THEN 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=1800&q=85'
        WHEN 1 THEN 'https://images.unsplash.com/photo-1530789253388-582c481c54b0?auto=format&fit=crop&w=1800&q=85'
        WHEN 2 THEN 'https://images.unsplash.com/photo-1488085061387-422e29b40080?auto=format&fit=crop&w=1800&q=85'
        ELSE 'https://images.unsplash.com/photo-1517760444937-f6397edcbbcd?auto=format&fit=crop&w=1800&q=85' END,
    CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN '모바일 바우처 확인 → 지정 게이트 QR 인증 → 자유 관람'
        WHEN 1 THEN '미팅 포인트 집결 → 대표 명소 2곳 → 현지 추천 거리 → 투어 종료'
        WHEN 2 THEN '도심 집결 → 전망 포인트 → 일몰 감상 → 야경 명소 → 복귀'
        WHEN 3 THEN '전통시장 → 대표 음식 시식 → 로컬 디저트 → 추천 맛집 안내'
        WHEN 4 THEN '입국장 미팅 → 수하물 적재 → 숙소 또는 지정 장소 하차'
        WHEN 5 THEN 'QR 수신 → 단말기 설정 → 현지망 접속 → 기간 종료 시 자동 해지'
        WHEN 6 THEN '사전 콘셉트 상담 → 촬영 장소 2곳 → 원본 선별 → 보정본 전달'
        WHEN 7 THEN '숙소 픽업 → 가족 선호 명소 → 점심 자유시간 → 체험 장소 → 복귀'
        WHEN 8 THEN '패스 활성화 → 지정 교통수단 자유 이용 → 유효기간 종료'
        ELSE '숙소 픽업 → 맞춤 명소 3곳 → 현지 식사 → 자유 일정 → 숙소 복귀' END,
    CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN '입장권, QR 모바일 바우처, 현지 고객 지원'
        WHEN 1 THEN '전문 가이드, 일정 내 이동, 기본 입장료, 생수'
        WHEN 2 THEN '왕복 이동, 인솔자, 웰컴 드링크, 여행자 보험'
        WHEN 3 THEN '현지 가이드, 음식 4종, 음료 1잔, 시장 안내 지도'
        WHEN 4 THEN '전용 차량, 전문 기사, 유류비, 통행료, 무료 대기 60분'
        WHEN 5 THEN 'eSIM 프로파일, 선택 데이터, 핫스팟, 온라인 설치 지원'
        WHEN 6 THEN '전문 촬영, 원본 50장 이상, 보정본 10장, 온라인 전달'
        WHEN 7 THEN '전용 차량, 가족 가이드, 카시트 1개, 생수'
        WHEN 8 THEN '지정 교통수단 이용권, 노선도, 모바일 패스'
        ELSE '전용 차량, 전담 가이드, 일정 컨설팅, 생수, 기본 보험' END,
    CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN '호텔 픽업, 개인 식비, 유료 체험, 개인 경비'
        WHEN 1 THEN '식사, 개인 구매, 선택 관광, 가이드 팁'
        WHEN 2 THEN '저녁 식사, 주류, 개인 촬영 장비, 개인 경비'
        WHEN 3 THEN '추가 주문, 주류, 호텔 이동, 개인 구매'
        WHEN 4 THEN '무료 대기 초과 요금, 추가 경유지, 유아용 추가 카시트'
        WHEN 5 THEN '통화 및 문자, 단말기, 초과 데이터, 로밍 요금'
        WHEN 6 THEN '의상 및 메이크업, 스튜디오 대관, 추가 보정본'
        WHEN 7 THEN '입장권, 식사, 유료 체험, 추가 카시트'
        WHEN 8 THEN '공항철도 특별석, 관광지 입장료, 좌석 예약 요금'
        ELSE '식사, 유료 입장권, 시간 초과 비용, 개인 경비' END,
    CASE MOD(d.RN - 1, 3)
        WHEN 0 THEN '이용일 하루 전 바우처를 확인해 주세요. 모바일 화면 또는 출력본을 제시하면 됩니다.'
        WHEN 1 THEN '예약 확정 후 안내되는 메신저 채널을 등록하고, 시작 10분 전까지 미팅 장소에 도착해 주세요.'
        ELSE '결제 즉시 발송되는 QR을 확인하고 이용일에 맞춰 활성화하세요. 주문 정보는 여권 영문명과 같아야 합니다.' END,
    CONCAT('여권 또는 신분증을 준비해 주세요. 현지 기상·교통 상황에 따라 순서가 변경될 수 있습니다. ',
           '만 12세 미만은 보호자 동반이 필요하며, 특별한 이동 지원이 필요하면 예약 메모에 남겨 주세요.'),
    CASE MOD(d.RN - 1, 4)
        WHEN 0 THEN '이용 72시간 전까지 무료 취소, 이후 환불 불가입니다.'
        WHEN 1 THEN '이용 7일 전까지 전액, 3일 전까지 50% 환불되며 이후에는 환불되지 않습니다.'
        WHEN 2 THEN '예약 확정 전 무료 취소가 가능하며 확정 후에는 일정 변경 1회만 가능합니다.'
        ELSE '이용 24시간 전까지 무료 취소가 가능하며 노쇼 및 지각은 환불되지 않습니다.' END,
    CONCAT('Q. 예약 즉시 확정되나요?\nA. 대부분 5분 이내 확정되며 별도 확인 상품은 24시간 안에 안내됩니다.\n',
           'Q. 날짜를 변경할 수 있나요?\nA. 재고가 있는 경우 이용 48시간 전까지 1회 가능합니다.\n',
           'Q. 어린이도 예약해야 하나요?\nA. 옵션별 연령 기준을 확인해 주세요.'),
    CASE MOD(d.RN - 1, 5)
        WHEN 0 THEN '09:00' WHEN 1 THEN '10:30' WHEN 2 THEN '16:30'
        WHEN 3 THEN '18:00' ELSE '항공편 도착 시간 또는 예약 시간' END,
    CASE MOD(d.RN - 1, 5)
        WHEN 0 THEN CONCAT(d.DEST_NAME, ' 메인 입구 안내 데스크')
        WHEN 1 THEN CONCAT(d.DEST_NAME, ' 인근 중앙역 2번 출구')
        WHEN 2 THEN CONCAT(d.DEST_NAME, ' 방문자 센터 앞')
        WHEN 3 THEN CONCAT(d.DEST_NAME, ' 구시가지 광장 시계탑')
        ELSE '예약 확정 후 바우처에 개별 안내' END,
    CONCAT('https://demo.routemate.local/products/', d.DEST_ID, '-', d.RN),
    CASE MOD(d.RN - 1, 10)
        WHEN 0 THEN 19000 + d.RN * 500 WHEN 1 THEN 49000 + d.RN * 700
        WHEN 2 THEN 59000 + d.RN * 650 WHEN 3 THEN 39000 + d.RN * 550
        WHEN 4 THEN 45000 + d.RN * 800 WHEN 5 THEN 9900 + MOD(d.RN, 8) * 1500
        WHEN 6 THEN 79000 + d.RN * 900 WHEN 7 THEN 119000 + d.RN * 1200
        WHEN 8 THEN 15000 + d.RN * 350 ELSE 169000 + d.RN * 1500 END,
    'KRW', 'Y', d.RN
FROM (
    SELECT ranked.DEST_ID, ranked.DEST_NAME, ranked.RN
    FROM (
        SELECT DEST_ID, DEST_NAME, ROW_NUMBER() OVER (ORDER BY DEST_ID) AS RN
        FROM TB_DESTINATION
    ) ranked
    WHERE ranked.RN <= 100
) d
WHERE NOT EXISTS (
    SELECT 1 FROM TB_TRAVEL_PRODUCT p
    WHERE p.BOOKING_URL = CONCAT('https://demo.routemate.local/products/', d.DEST_ID, '-', d.RN)
);

INSERT INTO TB_TRAVEL_PRODUCT_OPTION (
    PRODUCT_ID, OPTION_NAME, OPTION_DESC, PRICE, CURRENCY,
    CANCELLATION_POLICY, VALIDITY_TEXT, CONFIRMATION_TYPE, USE_YN, SORT_ORDER
)
SELECT
    p.PRODUCT_ID,
    CASE o.OPTION_NO
        WHEN 1 THEN '스탠다드 · 성인 1인'
        WHEN 2 THEN '라이트 · 아동/청소년 1인'
        ELSE CASE p.PRODUCT_TYPE
            WHEN 'TICKET' THEN '패스트트랙 · 성인 1인'
            WHEN 'TOUR' THEN '프리미엄 · 소그룹 1인'
            WHEN 'TRANSFER' THEN '패밀리 · 최대 4인'
            WHEN 'SIM' THEN '플러스 · 데이터 2배'
            ELSE '프리미엄 · 추가 혜택 포함' END
    END,
    CASE o.OPTION_NO
        WHEN 1 THEN '기본 구성과 필수 혜택이 모두 포함된 대표 옵션입니다.'
        WHEN 2 THEN '만 3~17세 이용자를 위한 할인 옵션이며 현장에서 나이를 확인할 수 있습니다.'
        ELSE '대기 단축, 전용 이동, 추가 데이터 또는 우선 지원 등 상품 유형별 프리미엄 혜택이 포함됩니다.'
    END,
    CASE o.OPTION_NO
        WHEN 1 THEN p.PRICE
        WHEN 2 THEN ROUND(p.PRICE * 0.72, -2)
        ELSE ROUND(p.PRICE * CASE WHEN p.PRODUCT_TYPE = 'TRANSFER' THEN 2.45 ELSE 1.45 END, -2)
    END,
    p.CURRENCY,
    CASE o.OPTION_NO
        WHEN 1 THEN '이용 72시간 전까지 무료 취소'
        WHEN 2 THEN '이용 48시간 전까지 무료 취소'
        ELSE '예약 확정 후 변경 가능, 이용 7일 전까지 무료 취소' END,
    CASE p.PRODUCT_TYPE
        WHEN 'SIM' THEN '설치 후 3일, 5일 또는 10일'
        WHEN 'TRANSFER' THEN '지정한 이용일과 시간에만 유효'
        ELSE '예약한 날짜의 운영시간 내 1회 사용' END,
    CASE WHEN MOD(p.PRODUCT_ID + o.OPTION_NO, 4) = 0 THEN 'MANUAL' ELSE 'INSTANT' END,
    'Y', o.OPTION_NO
FROM TB_TRAVEL_PRODUCT p
CROSS JOIN (
    SELECT 1 AS OPTION_NO UNION ALL SELECT 2 UNION ALL SELECT 3
) o
WHERE p.BOOKING_URL LIKE 'https://demo.routemate.local/products/%'
  AND NOT EXISTS (
      SELECT 1 FROM TB_TRAVEL_PRODUCT_OPTION existing_option
      WHERE existing_option.PRODUCT_ID = p.PRODUCT_ID
        AND existing_option.SORT_ORDER = o.OPTION_NO
  );
