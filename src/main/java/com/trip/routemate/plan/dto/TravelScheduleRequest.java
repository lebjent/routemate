package com.trip.routemate.plan.dto;

/**
 * 방문 지역 안의 시간대별 세부 일정과 선택 교통편을 등록하는 요청이다.
 *
 * @param time 일정 시작 시각 또는 시간대 표시
 * @param title 일정 제목
 * @param location 방문 장소
 * @param memo 사용자 메모
 * @param transportType 이동 수단 유형
 * @param transportName 교통편 이름 또는 노선
 * @param departureTime 출발 시각
 * @param arrivalTime 도착 시각
 * @param transportMemo 교통편 관련 메모
 * @param productOrderId 이 일정에 연결할 사용자 예약 식별자
 */
public record TravelScheduleRequest(
        String time,
        String title,
        String location,
        String memo,
        String transportType,
        String transportName,
        String departureTime,
        String arrivalTime,
        String transportMemo,
        Long productOrderId
) {
}
