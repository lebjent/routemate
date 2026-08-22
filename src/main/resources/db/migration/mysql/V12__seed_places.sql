-- 100 representative places distributed across the countries and regions from V2/V3.
-- Country and region codes are resolved at migration time so generated identifiers are never assumed.
INSERT INTO TB_DESTINATION (
    DEST_NAME, DEST_DESC, COUNTRY_ID, REGION_ID, CATEGORY,
    IMAGE_URL, MAP_LAT, MAP_LNG, LIKE_COUNT, CREATE_DT
)
SELECT
    s.DEST_NAME,
    CONCAT(s.DEST_NAME, '의 분위기와 매력을 즐길 수 있는 대표 플레이스입니다.'),
    c.COUNTRY_ID,
    r.REGION_ID,
    s.CATEGORY,
    CASE s.CATEGORY
        WHEN 'FOOD' THEN 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=900&q=80'
        WHEN 'SIGHTSEEING' THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80'
        WHEN 'SHOPPING' THEN 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=900&q=80'
        WHEN 'ACCOMMODATION' THEN 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80'
        WHEN 'CAFE' THEN 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=900&q=80'
        WHEN 'NATURE' THEN 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=80'
        WHEN 'CULTURE' THEN 'https://images.unsplash.com/photo-1561214115-f2f134cc4912?auto=format&fit=crop&w=900&q=80'
        WHEN 'ACTIVITY' THEN 'https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=900&q=80'
    END,
    s.MAP_LAT,
    s.MAP_LNG,
    100 + (s.SEED_NO * 17),
    CURRENT_TIMESTAMP(6)
FROM (
    SELECT 1 AS SEED_NO, 'KR' AS COUNTRY_CODE, 'SEL' AS REGION_CODE, '경복궁' AS DEST_NAME, 'SIGHTSEEING' AS CATEGORY, 37.5796 AS MAP_LAT, 126.977 AS MAP_LNG
    UNION ALL
    SELECT 2, 'KR', 'SEL', '남산서울타워', 'SIGHTSEEING', 37.5512, 126.9882
    UNION ALL
    SELECT 3, 'KR', 'SEL', '광장시장', 'FOOD', 37.57, 126.9996
    UNION ALL
    SELECT 4, 'KR', 'SEL', '성수동 카페거리', 'CAFE', 37.5446, 127.0557
    UNION ALL
    SELECT 5, 'KR', 'BUS', '해운대해수욕장', 'NATURE', 35.1587, 129.1604
    UNION ALL
    SELECT 6, 'KR', 'BUS', '감천문화마을', 'CULTURE', 35.0975, 129.0106
    UNION ALL
    SELECT 7, 'KR', 'CJU', '성산일출봉', 'NATURE', 33.4581, 126.9425
    UNION ALL
    SELECT 8, 'KR', 'CJU', '제주신화월드', 'ACTIVITY', 33.3048, 126.3172
    UNION ALL
    SELECT 9, 'JP', 'TYO', '시부야 스크램블 교차로', 'SIGHTSEEING', 35.6595, 139.7005
    UNION ALL
    SELECT 10, 'JP', 'TYO', '센소지', 'CULTURE', 35.7148, 139.7967
    UNION ALL
    SELECT 11, 'JP', 'TYO', '츠키지 장외시장', 'FOOD', 35.6655, 139.7708
    UNION ALL
    SELECT 12, 'JP', 'TYO', '스타벅스 리저브 로스터리 도쿄', 'CAFE', 35.6492, 139.6921
    UNION ALL
    SELECT 13, 'JP', 'OSA', '도톤보리', 'FOOD', 34.6687, 135.5013
    UNION ALL
    SELECT 14, 'JP', 'OSA', '오사카성', 'CULTURE', 34.6873, 135.5262
    UNION ALL
    SELECT 15, 'JP', 'KYO', '후시미이나리 신사', 'CULTURE', 34.9671, 135.7727
    UNION ALL
    SELECT 16, 'JP', 'KYO', '아라시야마 대나무숲', 'NATURE', 35.017, 135.6713
    UNION ALL
    SELECT 17, 'US', 'NYC', '타임스 스퀘어', 'SIGHTSEEING', 40.758, -73.9855
    UNION ALL
    SELECT 18, 'US', 'NYC', '센트럴 파크', 'NATURE', 40.7829, -73.9654
    UNION ALL
    SELECT 19, 'US', 'NYC', '메트로폴리탄 미술관', 'CULTURE', 40.7794, -73.9632
    UNION ALL
    SELECT 20, 'US', 'NYC', '첼시 마켓', 'FOOD', 40.7424, -74.0061
    UNION ALL
    SELECT 21, 'US', 'LAX', '산타모니카 피어', 'NATURE', 34.0094, -118.4973
    UNION ALL
    SELECT 22, 'US', 'LAX', '그리피스 천문대', 'SIGHTSEEING', 34.1184, -118.3004
    UNION ALL
    SELECT 23, 'US', 'SFO', '골든게이트 브리지', 'SIGHTSEEING', 37.8199, -122.4783
    UNION ALL
    SELECT 24, 'US', 'SFO', '페리 빌딩 마켓플레이스', 'FOOD', 37.7955, -122.3937
    UNION ALL
    SELECT 25, 'FR', 'PAR', '에펠탑', 'SIGHTSEEING', 48.8584, 2.2945
    UNION ALL
    SELECT 26, 'FR', 'PAR', '루브르 박물관', 'CULTURE', 48.8606, 2.3376
    UNION ALL
    SELECT 27, 'FR', 'PAR', '카페 드 플로르', 'CAFE', 48.854, 2.3325
    UNION ALL
    SELECT 28, 'FR', 'NCE', '영국인 산책로', 'NATURE', 43.695, 7.2656
    UNION ALL
    SELECT 29, 'FR', 'NCE', '호텔 네그레스코', 'ACCOMMODATION', 43.6945, 7.2583
    UNION ALL
    SELECT 30, 'FR', 'LYS', '푸르비에르 노트르담 대성당', 'CULTURE', 45.7623, 4.8226
    UNION ALL
    SELECT 31, 'FR', 'LYS', '레알 드 리옹 폴 보퀴즈', 'FOOD', 45.7616, 4.8505
    UNION ALL
    SELECT 32, 'IT', 'ROM', '콜로세움', 'SIGHTSEEING', 41.8902, 12.4922
    UNION ALL
    SELECT 33, 'IT', 'ROM', '바티칸 박물관', 'CULTURE', 41.9065, 12.4536
    UNION ALL
    SELECT 34, 'IT', 'ROM', '트레비 분수', 'SIGHTSEEING', 41.9009, 12.4833
    UNION ALL
    SELECT 35, 'IT', 'MIL', '밀라노 대성당', 'CULTURE', 45.4642, 9.19
    UNION ALL
    SELECT 36, 'IT', 'MIL', '비토리오 에마누엘레 2세 갤러리아', 'SHOPPING', 45.4659, 9.19
    UNION ALL
    SELECT 37, 'IT', 'VCE', '산마르코 광장', 'CULTURE', 45.4342, 12.3385
    UNION ALL
    SELECT 38, 'IT', 'VCE', '리알토 시장', 'FOOD', 45.438, 12.3359
    UNION ALL
    SELECT 39, 'ES', 'BCN', '사그라다 파밀리아', 'SIGHTSEEING', 41.4036, 2.1744
    UNION ALL
    SELECT 40, 'ES', 'BCN', '구엘 공원', 'NATURE', 41.4145, 2.1527
    UNION ALL
    SELECT 41, 'ES', 'BCN', '보케리아 시장', 'FOOD', 41.3817, 2.1716
    UNION ALL
    SELECT 42, 'ES', 'MAD', '프라도 미술관', 'CULTURE', 40.4138, -3.6921
    UNION ALL
    SELECT 43, 'ES', 'MAD', '그란비아', 'SHOPPING', 40.42, -3.7058
    UNION ALL
    SELECT 44, 'ES', 'SVQ', '세비야 알카사르', 'CULTURE', 37.383, -5.9902
    UNION ALL
    SELECT 45, 'ES', 'SVQ', '스페인 광장', 'SIGHTSEEING', 37.3772, -5.9869
    UNION ALL
    SELECT 46, 'GB', 'LON', '빅벤', 'SIGHTSEEING', 51.5007, -0.1246
    UNION ALL
    SELECT 47, 'GB', 'LON', '영국 박물관', 'CULTURE', 51.5194, -0.127
    UNION ALL
    SELECT 48, 'GB', 'LON', '버러 마켓', 'FOOD', 51.5055, -0.091
    UNION ALL
    SELECT 49, 'GB', 'LON', '코벤트 가든', 'SHOPPING', 51.5117, -0.124
    UNION ALL
    SELECT 50, 'GB', 'EDI', '에든버러성', 'CULTURE', 55.9486, -3.1999
    UNION ALL
    SELECT 51, 'GB', 'EDI', '아서스 시트', 'NATURE', 55.9441, -3.1618
    UNION ALL
    SELECT 52, 'TH', 'BKK', '방콕 왕궁', 'CULTURE', 13.75, 100.4913
    UNION ALL
    SELECT 53, 'TH', 'BKK', '왓 아룬', 'CULTURE', 13.7437, 100.4889
    UNION ALL
    SELECT 54, 'TH', 'BKK', '짜뚜짝 주말시장', 'SHOPPING', 13.7999, 100.55
    UNION ALL
    SELECT 55, 'TH', 'HKT', '빠통 비치', 'NATURE', 7.8966, 98.2966
    UNION ALL
    SELECT 56, 'TH', 'HKT', '푸껫 올드타운', 'CULTURE', 7.884, 98.3889
    UNION ALL
    SELECT 57, 'TH', 'CNX', '도이수텝 사원', 'CULTURE', 18.8048, 98.9216
    UNION ALL
    SELECT 58, 'TH', 'CNX', '치앙마이 나이트 바자', 'SHOPPING', 18.7878, 99.0004
    UNION ALL
    SELECT 59, 'VN', 'HAN', '호안끼엠 호수', 'NATURE', 21.0287, 105.8521
    UNION ALL
    SELECT 60, 'VN', 'HAN', '분짜 흐엉리엔', 'FOOD', 21.0182, 105.8532
    UNION ALL
    SELECT 61, 'VN', 'SGN', '벤탄시장', 'SHOPPING', 10.7725, 106.698
    UNION ALL
    SELECT 62, 'VN', 'SGN', '랜드마크 81', 'SIGHTSEEING', 10.795, 106.7219
    UNION ALL
    SELECT 63, 'VN', 'DAD', '미케 비치', 'NATURE', 16.0544, 108.2461
    UNION ALL
    SELECT 64, 'VN', 'DAD', '호이안 올드타운', 'CULTURE', 15.8801, 108.338
    UNION ALL
    SELECT 65, 'VN', 'PQC', '사오 비치', 'NATURE', 10.0575, 104.0358
    UNION ALL
    SELECT 66, 'VN', 'PQC', '푸꾸옥 야시장', 'FOOD', 10.2168, 103.9593
    UNION ALL
    SELECT 67, 'AU', 'SYD', '시드니 오페라 하우스', 'CULTURE', -33.8568, 151.2153
    UNION ALL
    SELECT 68, 'AU', 'SYD', '본다이 비치', 'NATURE', -33.8915, 151.2767
    UNION ALL
    SELECT 69, 'AU', 'MEL', '페더레이션 스퀘어', 'CULTURE', -37.8179, 144.9691
    UNION ALL
    SELECT 70, 'AU', 'MEL', '디그레이브스 스트리트 카페거리', 'CAFE', -37.8176, 144.9655
    UNION ALL
    SELECT 71, 'AU', 'BNE', '사우스뱅크 파크랜드', 'NATURE', -27.4789, 153.0235
    UNION ALL
    SELECT 72, 'AU', 'BNE', '론파인 코알라 보호구역', 'ACTIVITY', -27.533, 152.9688
    UNION ALL
    SELECT 73, 'SG', 'SIN', '마리나 베이 샌즈', 'ACCOMMODATION', 1.2834, 103.8607
    UNION ALL
    SELECT 74, 'SG', 'SIN', '가든스 바이 더 베이', 'NATURE', 1.2816, 103.8636
    UNION ALL
    SELECT 75, 'SG', 'SIN', '머라이언 파크', 'SIGHTSEEING', 1.2868, 103.8545
    UNION ALL
    SELECT 76, 'SG', 'SIN', '라우파삿', 'FOOD', 1.2806, 103.8505
    UNION ALL
    SELECT 77, 'SG', 'SIN', '오차드 로드', 'SHOPPING', 1.3048, 103.8318
    UNION ALL
    SELECT 78, 'SG', 'SIN', '센토사섬', 'ACTIVITY', 1.2494, 103.8303
    UNION ALL
    SELECT 79, 'TW', 'TPE', '타이베이 101', 'SIGHTSEEING', 25.034, 121.5645
    UNION ALL
    SELECT 80, 'TW', 'TPE', '국립고궁박물원', 'CULTURE', 25.1024, 121.5485
    UNION ALL
    SELECT 81, 'TW', 'TPE', '스린 야시장', 'FOOD', 25.0879, 121.524
    UNION ALL
    SELECT 82, 'TW', 'TPE', '시먼딩', 'SHOPPING', 25.042, 121.5079
    UNION ALL
    SELECT 83, 'TW', 'TPE', '베이터우 온천 리조트', 'ACCOMMODATION', 25.1367, 121.507
    UNION ALL
    SELECT 84, 'CN', 'PEK', '자금성', 'CULTURE', 39.9163, 116.3972
    UNION ALL
    SELECT 85, 'CN', 'PEK', '만리장성 팔달령', 'SIGHTSEEING', 40.3598, 116.02
    UNION ALL
    SELECT 86, 'CN', 'PEK', '왕푸징 거리', 'SHOPPING', 39.9134, 116.411
    UNION ALL
    SELECT 87, 'CN', 'SHA', '와이탄', 'SIGHTSEEING', 31.24, 121.49
    UNION ALL
    SELECT 88, 'CN', 'SHA', '예원', 'CULTURE', 31.227, 121.4921
    UNION ALL
    SELECT 89, 'CA', 'YVR', '스탠리 파크', 'NATURE', 49.3043, -123.1443
    UNION ALL
    SELECT 90, 'CA', 'YVR', '그랜빌 아일랜드 퍼블릭 마켓', 'FOOD', 49.2722, -123.134
    UNION ALL
    SELECT 91, 'CA', 'YVR', '캐필라노 서스펜션 브리지', 'ACTIVITY', 49.3429, -123.1149
    UNION ALL
    SELECT 92, 'CA', 'YYZ', 'CN 타워', 'SIGHTSEEING', 43.6426, -79.3871
    UNION ALL
    SELECT 93, 'CA', 'YYZ', '로열 온타리오 박물관', 'CULTURE', 43.6677, -79.3948
    UNION ALL
    SELECT 94, 'CA', 'YYZ', '세인트 로렌스 마켓', 'FOOD', 43.6487, -79.3716
    UNION ALL
    SELECT 95, 'DE', 'BER', '브란덴부르크 문', 'SIGHTSEEING', 52.5163, 13.3777
    UNION ALL
    SELECT 96, 'DE', 'BER', '박물관섬', 'CULTURE', 52.5169, 13.401
    UNION ALL
    SELECT 97, 'DE', 'BER', '이스트 사이드 갤러리', 'CULTURE', 52.505, 13.4397
    UNION ALL
    SELECT 98, 'DE', 'MUC', '마리엔 광장', 'SIGHTSEEING', 48.1374, 11.5755
    UNION ALL
    SELECT 99, 'DE', 'MUC', 'BMW 박물관', 'CULTURE', 48.177, 11.5591
    UNION ALL
    SELECT 100, 'DE', 'MUC', '영국정원', 'NATURE', 48.1642, 11.6056
) s
JOIN TB_COUNTRY c
  ON c.COUNTRY_CODE = s.COUNTRY_CODE
JOIN TB_REGION r
  ON r.COUNTRY_ID = c.COUNTRY_ID
 AND r.REGION_CODE = s.REGION_CODE
WHERE NOT EXISTS (
    SELECT 1
    FROM TB_DESTINATION d
    WHERE d.COUNTRY_ID = c.COUNTRY_ID
      AND d.REGION_ID = r.REGION_ID
      AND d.DEST_NAME = s.DEST_NAME
);

