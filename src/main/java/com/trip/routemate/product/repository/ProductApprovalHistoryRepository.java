package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.ProductApprovalHistory;
import com.trip.routemate.product.domain.TravelProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 상품 심사 이력을 최신 결정 순으로 조회하고 저장한다. */
public interface ProductApprovalHistoryRepository extends JpaRepository<ProductApprovalHistory, Long> {
    List<ProductApprovalHistory> findAllByProductOrderByDecisionDtDesc(TravelProduct product);
}
