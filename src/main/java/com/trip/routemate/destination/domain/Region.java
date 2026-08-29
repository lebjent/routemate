package com.trip.routemate.destination.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_REGION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/** 국가 안에서 여행지를 구분하는 지역 마스터 엔티티다. */
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REGION_ID")
    private Long regionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;

    @Column(name = "REGION_NAME", nullable = false, length = 50)
    private String regionName;

    @Column(name = "REGION_CODE", nullable = false, length = 20)
    private String regionCode;

    @Column(name = "SORT_ORDER")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "REGION_STAT_CD", nullable = false, length = 20)
    @Builder.Default
    private String regionStatCd = "ACTIVE";

    @CreationTimestamp
    @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;

    public void update(String regionName, String regionCode, Integer sortOrder, String regionStatCd) {
        this.regionName = regionName;
        this.regionCode = regionCode;
        this.sortOrder = sortOrder;
        this.regionStatCd = regionStatCd;
    }
}
