package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.order.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 응답(Response) DTO: 주문 항목 1건(OrderItem)을 클라이언트에게 내려줄 JSON 형태로 담는 그릇.
 * <p>
 * 엔티티(OrderItem)를 그대로 응답에 쓰지 않고 이 DTO로 변환해서 보낸다.
 * 이유: 엔티티에는 클라이언트가 알 필요 없는 연관관계/내부 필드가 많고,
 *       LAZY 연관을 직렬화하다 오류가 나거나 원치 않는 데이터까지 노출될 수 있기 때문.
 * 보통 주문 응답(OrderResponse) 안에 이 OrderItemResponse가 여러 개 리스트로 들어간다.
 */
@Getter                            // Lombok: 각 필드의 getter 자동 생성. JSON 직렬화기(Jackson)가 getter로 값을 읽어 응답을 만든다
@Builder                           // Lombok: 빌더 패턴 자동 생성 → 아래 from()에서 .필드명(값)... .build() 형태로 조립 가능
public class OrderItemResponse {

    private Long menuId;
    private String menuName;
    private int quantity;
    private int priceAtOrder;  // 주문 시점 메뉴 단가
    private int subtotal;      // (단가 + 옵션합) × 수량
    private List<OrderItemOptionResponse> options;   // 이 주문 항목에 선택된 옵션들. 각 옵션도 응답용 DTO로 변환해 담는다

    /**
     * 정적 팩토리 메서드: 엔티티(OrderItem) 한 건을 응답 DTO로 변환한다.
     * static 이라 객체를 먼저 만들 필요 없이 OrderItemResponse.from(item) 으로 바로 호출한다.
     * 보통 Service/Controller에서 조회한 엔티티를 응답으로 내보내기 직전에 이 메서드로 변환한다.
     */
    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .menuId(item.getMenu().getId())          // 연관된 메뉴(Menu)에서 PK와 이름만 꺼내 담는다 (엔티티 전체 노출 X)
                .menuName(item.getMenu().getName())
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .subtotal(item.getSubtotal())
                .options(item.getOptions().stream()      // 옵션 엔티티 리스트를 하나씩 순회하며
                        .map(OrderItemOptionResponse::from)   // 각각 OrderItemOptionResponse.from(옵션) 으로 변환 (메서드 참조)
                        .toList())                        // 변환 결과를 다시 List로 모은다
                .build();
    }
}
