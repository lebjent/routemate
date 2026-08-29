package com.trip.routemate.partner.repository;

import com.trip.routemate.partner.domain.PartnerCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 파트너사 사업자 정보 조회와 저장을 담당한다. */
public interface PartnerCompanyRepository extends JpaRepository<PartnerCompany, Long> {
    List<PartnerCompany> findAllByOrderByPartnerNameAsc();
    List<PartnerCompany> findAllByPartnerStatusOrderByPartnerNameAsc(String partnerStatus);
    Optional<PartnerCompany> findByPartnerCode(String partnerCode);
    Optional<PartnerCompany> findByBusinessNumber(String businessNumber);
}
