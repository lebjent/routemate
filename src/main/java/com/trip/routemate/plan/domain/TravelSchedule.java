package com.trip.routemate.plan.domain;

import com.trip.routemate.product.domain.ProductOrder;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_TRAVEL_SCHEDULE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_ID")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DAY_REGION_ID", nullable = false)
    private TravelDayRegion travelDayRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ORDER_ID")
    private ProductOrder productOrder;

    @Column(name = "SCHEDULE_TIME", length = 5)
    private String scheduleTime;

    @Column(name = "TITLE", length = 150)
    private String title;

    @Column(name = "LOCATION", length = 150)
    private String location;

    @Column(name = "MEMO", length = 500)
    private String memo;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;
}
