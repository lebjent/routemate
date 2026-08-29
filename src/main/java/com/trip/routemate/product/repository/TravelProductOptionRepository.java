package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 여행 옵션 상품의 판매 옵션을 조회하고 저장한다. */
public interface TravelProductOptionRepository extends JpaRepository<TravelProductOption, Long> {
    long countByUseYn(String useYn);

    List<TravelProductOption> findAllByProductOrderBySortOrderAscOptionIdAsc(TravelProduct product);
    List<TravelProductOption> findAllByProductInAndUseYnOrderByProductProductIdAscSortOrderAscOptionIdAsc(
            List<TravelProduct> products,
            String useYn
    );
    List<TravelProductOption> findAllByProductAndUseYnOrderBySortOrderAscOptionIdAsc(TravelProduct product, String useYn);
    Optional<TravelProductOption> findByOptionIdAndProductAndUseYn(Long optionId, TravelProduct product, String useYn);
    void deleteAllByProduct(TravelProduct product);
}
