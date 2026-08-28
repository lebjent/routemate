package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminProductOptionRequest;
import com.trip.routemate.admin.dto.AdminProductRequest;
import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.partner.domain.PartnerCompany;
import com.trip.routemate.partner.repository.PartnerCompanyRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock private TravelProductRepository productRepository;
    @Mock private DestinationRepository destinationRepository;
    @Mock private TravelProductOptionRepository optionRepository;
    @Mock private PartnerCompanyRepository partnerRepository;
    @InjectMocks private AdminProductService adminProductService;

    @Test
    void create_rejectsInactivePartnerConnection() {
        when(destinationRepository.findWithCountryAndRegionByDestId(10L))
                .thenReturn(Optional.of(Destination.builder().destId(10L).destName("서울").build()));
        when(partnerRepository.findById(20L)).thenReturn(Optional.of(PartnerCompany.builder()
                .partnerId(20L).partnerCode("PARTNER-001").partnerName("중지된 파트너")
                .partnerStatus("SUSPENDED").commissionRate(BigDecimal.ZERO).build()));

        assertThatThrownBy(() -> adminProductService.create(request(20L)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(productRepository);
    }

    private AdminProductRequest request(Long partnerId) {
        return new AdminProductRequest(10L, partnerId, "파트너 투어", "상품 요약", "TOUR", null,
                "상세 설명", null, null, null, null, null, null, null, null, null, null, null, null,
                BigDecimal.valueOf(50000), "KRW", "Y", 1, List.<AdminProductOptionRequest>of());
    }
}
