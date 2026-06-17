package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.order.Order;
import com.kuit.baemin.domain.order.OrderStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 주문 상태 변경 "응답" DTO.
 * 도메인 enum(OrderStatus)을 API 응답에 그대로 노출하지 않고, 얇은 DTO로 한 번 감싸 내려준다.
 * (다른 엔드포인트들이 모두 ...Response DTO로 응답하는 컨벤션에 맞춤 — 향후 enum 값이 바뀌어도 API 계약을 DTO에서 통제)
 */
@Getter   // Lombok이 각 필드의 getter를 자동 생성 (JSON 직렬화에 필요)
@Builder  // Lombok이 빌더 패턴(.builder().orderId(..)....build())을 자동 생성
public class OrderStatusResponse {

    private Long orderId;          // 어떤 주문인지
    private OrderStatus status;    // 변경된 현재 주문 상태

    // 엔티티(Order) -> 응답 DTO 변환용 static 팩토리 메서드 ('from' = 다른 타입 하나를 받아 이 타입으로 만든다는 관례적 이름)
    public static OrderStatusResponse from(Order order) {
        return OrderStatusResponse.builder()
                .orderId(order.getId())
                .status(order.getOrderStatus())
                .build();
    }
}
