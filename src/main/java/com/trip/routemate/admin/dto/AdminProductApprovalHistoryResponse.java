package com.trip.routemate.admin.dto;

import com.trip.routemate.product.domain.ProductApprovalHistory;

import java.time.LocalDateTime;

public record AdminProductApprovalHistoryResponse(String decisionStatus, String reason, String approverName, LocalDateTime decisionDt) {
    public static AdminProductApprovalHistoryResponse from(ProductApprovalHistory history) {
        return new AdminProductApprovalHistoryResponse(history.getDecisionStatus(), history.getDecisionReason(),
                history.getApprover().getUserNicknm(), history.getDecisionDt());
    }
}
