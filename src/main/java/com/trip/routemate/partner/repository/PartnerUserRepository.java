package com.trip.routemate.partner.repository;

import com.trip.routemate.partner.domain.PartnerCompany;
import com.trip.routemate.partner.domain.PartnerUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 파트너사와 대표·직원 계정의 소속 관계를 조회하고 저장한다. */
public interface PartnerUserRepository extends JpaRepository<PartnerUser, Long> {
    @EntityGraph(attributePaths = {"partner", "user"})
    Optional<PartnerUser> findByUserUserEmailAndUseYn(String userEmail, String useYn);

    @EntityGraph(attributePaths = {"user"})
    List<PartnerUser> findAllByPartnerOrderByPartnerRoleAscCreateDtAsc(PartnerCompany partner);

    @EntityGraph(attributePaths = {"partner", "user"})
    Optional<PartnerUser> findByPartnerUserIdAndPartner(Long partnerUserId, PartnerCompany partner);
}
