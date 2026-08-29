package com.trip.routemate.plan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "TB_TRAVEL_DAY",
        uniqueConstraints = @UniqueConstraint(name = "UK_TRAVEL_DAY_PLAN_DAY", columnNames = {"PLAN_ID", "DAY_NO"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/** 여행 계획에 속한 하루의 날짜와 일차 번호를 보관하는 엔티티다. */
public class TravelDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DAY_ID")
    private Long dayId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PLAN_ID", nullable = false)
    private TravelPlan travelPlan;

    @Column(name = "DAY_NO", nullable = false)
    private Integer dayNumber;

    @Column(name = "PLAN_DATE", nullable = false)
    private LocalDate planDate;
}
