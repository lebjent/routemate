package com.trip.routemate.destination.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DEST_RECOMMEND")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DestinationRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECOMMEND_ID")
    private Long recommendId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DEST_ID", nullable = false)
    private Destination destination;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "DISPLAY_START_DT", nullable = false)
    private LocalDateTime displayStartDt;

    @Column(name = "DISPLAY_END_DT", nullable = false)
    private LocalDateTime displayEndDt;

    @Column(name = "SORT_ORDER", nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String useYn = "Y";

    public void update(Destination destination, String imageUrl, LocalDateTime displayStartDt, LocalDateTime displayEndDt, Integer sortOrder, String useYn) {
        this.destination = destination;
        this.imageUrl = imageUrl;
        this.displayStartDt = displayStartDt;
        this.displayEndDt = displayEndDt;
        this.sortOrder = sortOrder;
        this.useYn = useYn;
    }
}
