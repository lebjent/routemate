package com.trip.routemate.product.repository;

import com.trip.routemate.product.domain.ProductPayment;
import com.trip.routemate.product.domain.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductPaymentRepository extends JpaRepository<ProductPayment, Long> {
    Optional<ProductPayment> findByOrder(ProductOrder order);
}
