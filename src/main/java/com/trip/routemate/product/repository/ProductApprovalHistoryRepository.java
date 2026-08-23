package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.ProductApprovalHistory;
import com.trip.routemate.product.domain.TravelProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductApprovalHistoryRepository extends JpaRepository<ProductApprovalHistory, Long> {
    List<ProductApprovalHistory> findAllByProductOrderByDecisionDtDesc(TravelProduct product);
}
