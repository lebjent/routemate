package com.trip.routemate.product.service;

import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.Region;
import com.trip.routemate.product.domain.ProductOrder;
import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;
import com.trip.routemate.product.dto.ProductOrderRequest;
import com.trip.routemate.product.repository.ProductOrderRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductOrderServiceTest {

    @Mock ProductOrderRepository orderRepository;
    @Mock TravelProductRepository productRepository;
    @Mock TravelProductOptionRepository optionRepository;
    @Mock UserMstrRepository userRepository;
    @InjectMocks ProductOrderService productOrderService;

    @Test
    void createOrder_usesTheCurrentServerOptionPriceAndStoresSnapshots() {
        var country = Country.builder().countryId(1L).countryName("대한민국").countryCode("KR").build();
        var region = Region.builder().regionId(2L).country(country).regionName("서울").regionCode("SEOUL").build();
        var destination = Destination.builder().destId(3L).country(country).region(region).destName("경복궁").build();
        var product = TravelProduct.builder().productId(4L).destination(destination).productName("경복궁 야간 투어")
                .imageUrl("https://example.com/product.jpg").useYn("Y").build();
        var option = TravelProductOption.builder().optionId(5L).product(product).optionName("성인 1인")
                .price(new BigDecimal("12300.00")).currency("KRW").useYn("Y").build();
        var user = UserMstr.builder().userId(6L).userEmail("buyer@example.com").userNicknm("구매자")
                .userStatCd("ACTIVE").delYn("N").build();
        var request = new ProductOrderRequest(4L, 5L, LocalDate.now().plusDays(2), 3,
                " 홍길동 ", "BUYER@EXAMPLE.COM", " 010-1234-5678 ");

        when(userRepository.findByUserEmail("buyer@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findWithDestinationByProductId(4L)).thenReturn(Optional.of(product));
        when(optionRepository.findByOptionIdAndProductAndUseYn(5L, product, "Y")).thenReturn(Optional.of(option));
        when(orderRepository.save(any(ProductOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productOrderService.createOrder("buyer@example.com", request);

        assertThat(response.unitPrice()).isEqualByComparingTo("12300.00");
        assertThat(response.totalPrice()).isEqualByComparingTo("36900.00");
        assertThat(response.paymentStatus()).isEqualTo("PENDING");
        assertThat(response.orderNo()).startsWith("RM").hasSize(34);

        var captor = ArgumentCaptor.forClass(ProductOrder.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getProductName()).isEqualTo("경복궁 야간 투어");
        assertThat(captor.getValue().getOptionName()).isEqualTo("성인 1인");
        assertThat(captor.getValue().getBuyerName()).isEqualTo("홍길동");
        assertThat(captor.getValue().getBuyerEmail()).isEqualTo("buyer@example.com");
    }
}
