package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminDestinationPlaceRequest;
import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.PlaceCategory;
import com.trip.routemate.destination.domain.Region;
import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDestinationServiceTest {

    @Mock private CountryRepository countryRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private DestinationRepository destinationRepository;
    @InjectMocks private AdminDestinationService adminDestinationService;

    @Test
    void createPlace_savesCategoryAndCountryRegionRelationship() {
        var country = country(1L, "대한민국");
        var region = region(10L, country, "서울");
        var request = new AdminDestinationPlaceRequest(" 광화문 ", "도심 관광지", 1L, 10L,
                PlaceCategory.SIGHTSEEING, null, 37.5759, 126.9768);

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(regionRepository.findByRegionIdAndCountry(10L, country)).thenReturn(Optional.of(region));
        when(destinationRepository.save(any(Destination.class)))
                .thenAnswer(invocation -> Objects.requireNonNull(invocation.<Destination>getArgument(0)));

        var result = adminDestinationService.createPlace(request);

        assertThat(result.destName()).isEqualTo("광화문");
        assertThat(result.countryId()).isEqualTo(1L);
        assertThat(result.regionId()).isEqualTo(10L);
        assertThat(result.category()).isEqualTo("SIGHTSEEING");
        assertThat(result.categoryLabel()).isEqualTo("관광지");
    }

    @Test
    void createPlace_rejectsRegionOutsideSelectedCountry() {
        var country = country(1L, "대한민국");
        var request = new AdminDestinationPlaceRequest("잘못된 플레이스", null, 1L, 20L,
                PlaceCategory.FOOD, null, 37.0, 127.0);

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(regionRepository.findByRegionIdAndCountry(20L, country)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminDestinationService.createPlace(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verifyNoInteractions(destinationRepository);
    }

    @Test
    void getPlaceCategories_returnsSupportedComboOptions() {
        var result = adminDestinationService.getPlaceCategories();

        assertThat(result.categories())
                .extracting("code")
                .containsExactly("FOOD", "SIGHTSEEING", "SHOPPING", "ACCOMMODATION", "CAFE", "NATURE", "CULTURE", "ACTIVITY");
    }

    private Country country(Long id, String name) {
        return Country.builder().countryId(id).countryName(name).countryCode("KR").build();
    }

    private Region region(Long id, Country country, String name) {
        return Region.builder().regionId(id).country(country).regionName(name).regionCode("SEOUL").build();
    }
}
