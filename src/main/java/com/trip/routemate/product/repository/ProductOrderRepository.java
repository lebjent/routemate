package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.ProductOrder;
import com.trip.routemate.user.domain.UserMstr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 사용자 예약 내역과 일정 연결 후보 조회를 담당한다. */
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findAllByUserOrderByCreateDtDescOrderIdDesc(UserMstr user);
    List<ProductOrder> findAllByOrderByCreateDtDescOrderIdDesc();

    @Query("""
            select productOrder from ProductOrder productOrder
            join fetch productOrder.product product
            join product.destination destination
            join destination.country country
            join destination.region region
            where productOrder.user = :user
              and productOrder.useDate = :useDate
              and country.countryCode = :countryCode
              and region.regionCode = :regionCode
              and productOrder.orderStatus <> 'CANCELLED'
            order by productOrder.createDt desc, productOrder.orderId desc
            """)
    List<ProductOrder> findScheduleCandidatesByUserAndDestination(
            @org.springframework.data.repository.query.Param("user") UserMstr user,
            @org.springframework.data.repository.query.Param("countryCode") String countryCode,
            @org.springframework.data.repository.query.Param("regionCode") String regionCode,
            @org.springframework.data.repository.query.Param("useDate") LocalDate useDate
    );

    List<ProductOrder> findTop5ByOrderByCreateDtDescOrderIdDesc();

    long countByPaymentStatus(String paymentStatus);

    @Query("select coalesce(sum(productOrder.totalPrice), 0) from ProductOrder productOrder where productOrder.paymentStatus = 'PAID'")
    BigDecimal getPaidRevenue();

    long countByProductPartner(com.trip.routemate.partner.domain.PartnerCompany partner);

    @Query("select coalesce(sum(productOrder.totalPrice), 0) from ProductOrder productOrder where productOrder.product.partner = :partner and productOrder.paymentStatus = 'PAID'")
    BigDecimal getPaidRevenueByPartner(@org.springframework.data.repository.query.Param("partner") com.trip.routemate.partner.domain.PartnerCompany partner);
}
