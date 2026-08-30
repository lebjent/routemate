package com.trip.routemate.partner.dto;

/** 파트너 상품 대표 이미지 업로드 후 DB에 저장할 URL을 전달하는 응답이다. */
public record PartnerProductImageUploadResponse(String imageUrl) {
}
