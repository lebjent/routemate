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

    @Lob
    @Column(name = "DEST_DESC")
    private String destDesc;

    @Column(name = "COUNTRY", nullable = false, length = 50)
    private String country;

    @Column(name = "CITY", nullable = false, length = 50)
    private String city;

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
}
