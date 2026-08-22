package com.trip.routemate.global.config;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Region;
import com.trip.routemate.destination.domain.PlaceCategory;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final DestinationRepository destinationRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final TravelPlanRepository travelPlanRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("DataInitializer: 초기 더미 데이터 검사 및 로딩 시작...");

        // 1. 여행지 더미 데이터 삽입
        if (countryRepository.count() == 0 && regionRepository.count() == 0 && destinationRepository.count() == 0) {
            log.info("Destination 데이터가 비어 있습니다. 더미 데이터를 저장합니다.");
            Country france = countryRepository.save(Country.builder()
                    .countryName("프랑스")
                    .countryCode("FR")
                    .build());
            Country japan = countryRepository.save(Country.builder()
                    .countryName("일본")
                    .countryCode("JP")
                    .build());
            Country usa = countryRepository.save(Country.builder()
                    .countryName("미국")
                    .countryCode("US")
                    .build());

            Region paris = regionRepository.save(Region.builder()
                    .country(france)
                    .regionName("파리")
                    .regionCode("PAR")
                    .sortOrder(1)
                    .build());
            Region tokyo = regionRepository.save(Region.builder()
                    .country(japan)
                    .regionName("도쿄")
                    .regionCode("TYO")
                    .sortOrder(1)
                    .build());
            Region newYork = regionRepository.save(Region.builder()
                    .country(usa)
                    .regionName("뉴욕")
                    .regionCode("NYC")
                    .sortOrder(1)
                    .build());

            destinationRepository.save(Destination.builder()
                    .destName("에펠탑")
                    .destDesc("프랑스 파리의 상징적인 철탑으로 전 세계 여행객이 방문하는 랜드마크입니다.")
                    .country(france)
                    .region(paris)
                    .category(PlaceCategory.SIGHTSEEING)
                    .imageUrl("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=600&q=80")
                    .mapLat(48.8584)
                    .mapLng(2.2945)
                    .likeCount(1500)
                    .build());

            destinationRepository.save(Destination.builder()
                    .destName("센소지")
                    .destDesc("도쿄에서 가장 오래된 절로 전통적인 일본의 멋을 느낄 수 있는 곳입니다.")
                    .country(japan)
                    .region(tokyo)
                    .category(PlaceCategory.CULTURE)
                    .imageUrl("https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=600&q=80")
                    .mapLat(35.7148)
                    .mapLng(139.7967)
                    .likeCount(1200)
                    .build());

            destinationRepository.save(Destination.builder()
                    .destName("타임스 스퀘어")
                    .destDesc("미국 뉴욕 맨해튼의 중심부로 화려한 광고판과 문화의 용광로입니다.")
                    .country(usa)
                    .region(newYork)
                    .category(PlaceCategory.SHOPPING)
                    .imageUrl("https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=600&q=80")
                    .mapLat(40.7580)
                    .mapLng(-73.9855)
                    .likeCount(2300)
                    .build());
        }

        // 2. 여행 일정(루트) 더미 데이터 삽입
        if (travelPlanRepository.count() == 0) {
            log.info("TravelPlan 데이터가 비어 있습니다. 더미 데이터를 저장합니다.");
            travelPlanRepository.save(TravelPlan.builder()
                    .userNicknm("파리러버")
                    .title("에펠탑에서 몽마르뜨까지, 감성 도보 힐링 루트")
                    .description("대중교통 환승 동선과 골목길 카페 명소까지 완벽하게 매칭된 3박 4일 파리 에센셜 코스")
                    .imageUrl("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=600&q=80")
                    .spotCount(12)
                    .likeCount(1420)
                    .isPublic("Y")
                    .build());

            travelPlanRepository.save(TravelPlan.builder()
                    .userNicknm("도쿄매니아")
                    .title("시부야 밤거리부터 아키하바라까지 테크&컬처 투어")
                    .description("지하철 복잡한 환승 레이어 연산으로 웨이팅과 동선 낭비를 원천 차단한 도심 밀착형 코스")
                    .imageUrl("https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=600&q=80")
                    .spotCount(18)
                    .likeCount(982)
                    .isPublic("Y")
                    .build());

            travelPlanRepository.save(TravelPlan.builder()
                    .userNicknm("뉴욕커스")
                    .title("맨해튼 스카이라인과 브로드웨이 뮤지컬 올패스 루트")
                    .description("실시간 타임라인 시뮬레이션 기반으로 예약을 놓치지 않게 설계된 뉴욕 인텐시브 투어")
                    .imageUrl("https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=600&q=80")
                    .spotCount(15)
                    .likeCount(2154)
                    .isPublic("Y")
                    .build());
        }

        log.info("DataInitializer: 초기 더미 데이터 검사 및 로딩 완료.");
    }
}
