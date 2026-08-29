package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.TravelProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** 관리자·파트너·공개 카탈로그에서 사용하는 여행 옵션 상품 조회를 담당한다. */
public interface TravelProductRepository extends JpaRepository<TravelProduct, Long> {
    interface ProductTypeStatView {
        String getProductType();
        Long getTotalCount();
        Long getActiveCount();
    }

    interface PartnerProductCountView {
        Long getPartnerId();
        Long getTotalProducts();
        Long getActiveProducts();
        Long getPendingProducts();
    }

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region", "partner"})
    List<TravelProduct> findAllByOrderBySortOrderAscCreateDtDesc();

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region", "partner"})
    Optional<TravelProduct> findWithDestinationByProductId(Long productId);

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region", "partner"})
    List<TravelProduct> findAllByUseYnOrderBySortOrderAscCreateDtDesc(String useYn);

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region", "partner"})
    List<TravelProduct> findAllByPartnerOrderByCreateDtDesc(com.trip.routemate.partner.domain.PartnerCompany partner);

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region", "partner"})
    @Query("""
            select product
              from TravelProduct product
             where product.useYn = 'Y'
               and product.approvalStatus = 'APPROVED'
               and (product.partner is null or product.partner.partnerStatus = 'ACTIVE')
             order by product.sortOrder, product.createDt desc
            """)
    List<TravelProduct> findPublicProducts();

    long countByUseYn(String useYn);

    @Query("""
            select product.productType as productType,
                   count(product) as totalCount,
                   sum(case when product.useYn = 'Y' then 1 else 0 end) as activeCount
              from TravelProduct product
             group by product.productType
             order by count(product) desc, product.productType
            """)
    List<ProductTypeStatView> findProductTypeStats();

    @Query("""
            select product.partner.partnerId as partnerId,
                   count(product) as totalProducts,
                   sum(case when product.useYn = 'Y' and product.approvalStatus = 'APPROVED' then 1 else 0 end) as activeProducts,
                   sum(case when product.approvalStatus = 'PENDING' then 1 else 0 end) as pendingProducts
              from TravelProduct product
             where product.partner is not null
             group by product.partner.partnerId
            """)
    List<PartnerProductCountView> findPartnerProductCounts();
}
