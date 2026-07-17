package com.trip.routemate.plan.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_TRAVEL_PLAN")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAN_ID")
    private Long planId;

    @Column(name = "USER_NICKNM", nullable = false, length = 50)
    private String userNicknm;

    @Column(name = "TITLE", nullable = false, length = 150)
    private String title;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "SPOT_COUNT")
    @Builder.Default
    private Integer spotCount = 0;

    @Column(name = "LIKE_COUNT")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "IS_PUBLIC", nullable = false, length = 1)
    @Builder.Default
    private String isPublic = "Y";

    @CreationTimestamp
    @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;

    @UpdateTimestamp
    @Column(name = "MDFY_DT", nullable = false)
    private LocalDateTime mdfyDt;
}
