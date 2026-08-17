package com.trip.routemate.destination.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DESTINATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEST_ID")
    private Long destId;

    @Column(name = "DEST_NAME", nullable = false, length = 100)
    private String destName;

    @Column(name = "DEST_DESC", columnDefinition = "LONGTEXT")
    private String destDesc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REGION_ID", nullable = false)
    private Region region;

    @Column(name = "CATEGORY", nullable = false, length = 30)
    private String category;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "MAP_LAT", nullable = false)
    private Double mapLat;

    @Column(name = "MAP_LNG", nullable = false)
    private Double mapLng;

    @Column(name = "LIKE_COUNT")
    @Builder.Default
    private Integer likeCount = 0;

    @CreationTimestamp
    @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;

    public void update(String destName, String destDesc, Country country, Region region, String category, String imageUrl, Double mapLat, Double mapLng) {
        this.destName = destName;
        this.destDesc = destDesc;
        this.country = country;
        this.region = region;
        this.category = category;
        this.imageUrl = imageUrl;
        this.mapLat = mapLat;
        this.mapLng = mapLng;
    }
}
