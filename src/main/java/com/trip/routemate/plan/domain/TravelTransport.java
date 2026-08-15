package com.trip.routemate.plan.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "TB_TRAVEL_TRANSPORT",
        uniqueConstraints = @UniqueConstraint(name = "UK_TRAVEL_TRANSPORT_SCHEDULE", columnNames = "SCHEDULE_ID")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelTransport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSPORT_ID")
    private Long transportId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SCHEDULE_ID", nullable = false, unique = true)
    private TravelSchedule travelSchedule;

    @Column(name = "TRANSPORT_TYPE", nullable = false, length = 20)
    private String transportType;

    @Column(name = "TRANSPORT_NAME", length = 100)
    private String transportName;

    @Column(name = "DEPARTURE_TIME", length = 5)
    private String departureTime;

    @Column(name = "ARRIVAL_TIME", length = 5)
    private String arrivalTime;

    @Column(name = "TRANSPORT_MEMO", length = 500)
    private String transportMemo;
}
