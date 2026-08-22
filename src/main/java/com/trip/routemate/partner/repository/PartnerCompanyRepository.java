package com.trip.routemate.partner.repository;

import com.trip.routemate.partner.domain.PartnerCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerCompanyRepository extends JpaRepository<PartnerCompany, Long> {
    List<PartnerCompany> findAllByOrderByPartnerNameAsc();
    List<PartnerCompany> findAllByPartnerStatusOrderByPartnerNameAsc(String partnerStatus);
    Optional<PartnerCompany> findByPartnerCode(String partnerCode);
    Optional<PartnerCompany> findByBusinessNumber(String businessNumber);
}
