package com.trip.routemate.destination.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_COUNTRY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/** 지역과 여행지가 소속되는 국가 마스터 엔티티다. */
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUNTRY_ID")
    private Long countryId;

    @Column(name = "COUNTRY_NAME", nullable = false, unique = true, length = 50)
    private String countryName;

    @Column(name = "COUNTRY_CODE", nullable = false, unique = true, length = 10)
    private String countryCode;

    @Column(name = "COUNTRY_STAT_CD", nullable = false, length = 20)
    @Builder.Default
    private String countryStatCd = "ACTIVE";

    @CreationTimestamp
    @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;

    public void update(String countryName, String countryCode, String countryStatCd) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.countryStatCd = countryStatCd;
    }
}
