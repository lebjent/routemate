package com.trip.routemate.product.domain;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.partner.domain.PartnerCompany;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_TRAVEL_PRODUCT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DEST_ID", nullable = false)
    private Destination destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTNER_ID")
    private PartnerCompany partner;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 150)
    private String productName;

    @Column(name = "PRODUCT_SUMMARY", length = 300)
    private String productSummary;

    @Column(name = "PRODUCT_TYPE", nullable = false, length = 30)
    private String productType;

    @Column(name = "PROVIDER_NAME", length = 100)
    private String providerName;

    @Column(name = "REGISTRATION_SOURCE", nullable = false, length = 20)
    @Builder.Default
    private String registrationSource = "ADMIN";

    @Column(name = "APPROVAL_STATUS", nullable = false, length = 20)
    @Builder.Default
    private String approvalStatus = "APPROVED";

    @Column(name = "APPROVAL_MEMO", length = 500)
    private String approvalMemo;

    @Column(name = "SUBMIT_DT")
    private LocalDateTime submitDt;

    @Column(name = "APPROVE_DT")
    private LocalDateTime approveDt;

    @Column(name = "PRODUCT_DESC", length = Length.LONG32)
    private String productDesc;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "DETAIL_IMAGE_URL", length = 500)
    private String detailImageUrl;

    @Column(name = "COURSE_TEXT", length = Length.LONG32)
    private String courseText;
    @Column(name = "INCLUDED_TEXT", length = Length.LONG32)
    private String includedText;
    @Column(name = "EXCLUDED_TEXT", length = Length.LONG32)
    private String excludedText;
    @Column(name = "USAGE_GUIDE_TEXT", length = Length.LONG32)
    private String usageGuideText;
    @Column(name = "NOTICE_TEXT", length = Length.LONG32)
    private String noticeText;
    @Column(name = "CANCELLATION_POLICY_TEXT", length = Length.LONG32)
    private String cancellationPolicyText;
    @Column(name = "FAQ_TEXT", length = Length.LONG32)
    private String faqText;
    @Column(name = "MEETING_TIME", length = 100)
    private String meetingTime;
    @Column(name = "MEETING_PLACE", length = 300)
    private String meetingPlace;

    @Column(name = "BOOKING_URL", length = 1000)
    private String bookingUrl;

    @Column(name = "PRICE", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    @Builder.Default
    private String currency = "KRW";

    @Column(name = "USE_YN", nullable = false, length = 1)
    @Builder.Default
    private String useYn = "Y";

    @Column(name = "SORT_ORDER", nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;

    @CreationTimestamp
    @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;

    public void update(Destination destination, PartnerCompany partner, String productName, String productSummary, String productType, String providerName,
                       String productDesc, String imageUrl, String detailImageUrl, String courseText, String includedText,
                       String excludedText, String usageGuideText, String noticeText, String cancellationPolicyText,
                       String faqText, String meetingTime, String meetingPlace, String bookingUrl, BigDecimal price,
                       String currency, String useYn, Integer sortOrder) {
        this.destination = destination;
        this.partner = partner;
        this.productName = productName;
        this.productSummary = productSummary;
        this.productType = productType;
        this.providerName = providerName;
        this.productDesc = productDesc;
        this.imageUrl = imageUrl;
        this.detailImageUrl = detailImageUrl;
        this.courseText = courseText;
        this.includedText = includedText;
        this.excludedText = excludedText;
        this.usageGuideText = usageGuideText;
        this.noticeText = noticeText;
        this.cancellationPolicyText = cancellationPolicyText;
        this.faqText = faqText;
        this.meetingTime = meetingTime;
        this.meetingPlace = meetingPlace;
        this.bookingUrl = bookingUrl;
        this.price = price;
        this.currency = currency;
        this.useYn = useYn;
        this.sortOrder = sortOrder;
    }
}
