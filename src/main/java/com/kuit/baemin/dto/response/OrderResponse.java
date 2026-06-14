package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.order.Order;
import com.kuit.baemin.domain.order.OrderItem;
import com.kuit.baemin.domain.order.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 "응답" DTO.
 * 클라이언트(앱/브라우저)에게 주문 1건을 내려줄 때 쓰는 그릇이다.
 * Order 엔티티를 그대로 노출하지 않고, 화면에 필요한 값만 골라(가게명/배달주소/금액 등) 평평하게 펼쳐 담는다.
 * (엔티티를 직접 응답하면 연관관계 지연로딩·민감정보 노출 등 문제가 생길 수 있어 DTO로 분리)
 * 한 주문 안의 여러 메뉴 줄은 items(OrderItemResponse 목록)로, 그 안의 옵션은 다시 OrderItemResponse가 품는 중첩 구조다.
 */
@Getter   // Lombok이 각 필드의 getter(getId() 등)를 자동 생성 (JSON으로 직렬화할 때 필요)
@Builder  // Lombok이 빌더 패턴(.builder().id(..)....build())을 자동 생성
public class OrderResponse {

    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private Long addressId;
    private String deliveryAddress;
    private OrderStatus orderStatus;
    private int itemsTotal;    // 메뉴/옵션 합계
    private int deliveryFee;   // 배달비
    private int totalPrice;    // 최종 결제 금액 (itemsTotal + deliveryFee)
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;  // 주문에 담긴 메뉴 줄 목록 (각 줄은 OrderItemResponse로 변환)

    // 엔티티(Order) -> 응답 DTO(OrderResponse) 변환용 static 팩토리 메서드.
    // 'from'은 "다른 타입 하나를 받아 이 타입으로 만든다"는 관례적 이름.
    // 컨트롤러/서비스에서 OrderResponse.from(order) 형태로 호출한다.
    public static OrderResponse from(Order order) {
        // 주문에 담긴 모든 메뉴 줄(OrderItem)의 subtotal을 더해 "메뉴/옵션 합계"를 계산한다.
        int itemsTotal = order.getOrderItems().stream()  // 메뉴 줄 목록을 스트림으로
                .mapToInt(OrderItem::getSubtotal)         // 각 줄을 그 줄의 subtotal(int)로 변환
                .sum();                                   // 모두 더해 합계 산출
        return OrderResponse.builder()
                .id(order.getId())
                .restaurantId(order.getRestaurant().getId())       // 연관된 가게(Restaurant)에서 PK만 꺼내옴
                .restaurantName(order.getRestaurant().getName())   // 가게 이름
                .addressId(order.getAddress().getId())             // 연관된 배달주소(Address)에서 PK만 꺼내옴
                .deliveryAddress(order.getAddress().getRoadAddress())  // 배달지의 도로명 주소
                .orderStatus(order.getOrderStatus())
                .itemsTotal(itemsTotal)                            // 위에서 계산한 메뉴/옵션 합계
                .deliveryFee(order.getDeliveryFee())                   // 배달비는 주문 시점 박제값에서 가져옴(가게의 현재값이 아님)
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)  // 메뉴 줄(OrderItem) 하나하나를 응답용 OrderItemResponse로 변환
                        .toList())
                .build();
    }
}
