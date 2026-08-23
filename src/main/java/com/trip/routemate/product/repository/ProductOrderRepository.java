package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.ProductOrder;
import com.trip.routemate.user.domain.UserMstr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findAllByUserOrderByCreateDtDescOrderIdDesc(UserMstr user);

    List<ProductOrder> findTop5ByOrderByCreateDtDescOrderIdDesc();

    long countByPaymentStatus(String paymentStatus);

    @Query("select coalesce(sum(productOrder.totalPrice), 0) from ProductOrder productOrder where productOrder.paymentStatus = 'PAID'")
    BigDecimal getPaidRevenue();

    long countByProductPartner(com.trip.routemate.partner.domain.PartnerCompany partner);

    @Query("select coalesce(sum(productOrder.totalPrice), 0) from ProductOrder productOrder where productOrder.product.partner = :partner and productOrder.paymentStatus = 'PAID'")
    BigDecimal getPaidRevenueByPartner(@org.springframework.data.repository.query.Param("partner") com.trip.routemate.partner.domain.PartnerCompany partner);
}
