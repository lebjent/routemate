package com.trip.routemate.plan.domain;

import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Region;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_TRAVEL_DAY_REGION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelDayRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DAY_REGION_ID")
    private Long dayRegionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DAY_ID", nullable = false)
    private TravelDay travelDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REGION_ID", nullable = false)
    private Region region;

    @Column(name = "REGION_NOTE", length = 500)
    private String regionNote;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;
}
