package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.order.OrderItemOption;
import lombok.Builder;
import lombok.Getter;

/**
 * 주문 항목에 선택된 "옵션 하나"를 클라이언트(앱/웹)에 돌려줄 때 쓰는 응답 DTO.
 * (예: 치킨 주문에서 고른 "양념 추가", "콜라 변경" 같은 개별 옵션 한 줄)
 *
 * DTO(Data Transfer Object)는 화면에 내려줄 데이터만 골라 담는 "전송 전용 그릇"이다.
 * 엔티티(OrderItemOption)를 그대로 노출하지 않고, 필요한 값만 추려 담아 응답한다.
 */
@Getter                 // Lombok: 각 필드의 getter(getOptionId 등)를 컴파일 시 자동 생성. JSON 직렬화 때 이 getter로 값을 꺼낸다.
@Builder                // Lombok: 빌더 패턴(.builder()...build())으로 객체를 만들 수 있게 해줌. 아래 from()에서 사용.
public class OrderItemOptionResponse {

    private Long optionId;          // 옵션의 식별자(메뉴 옵션 PK)
    private String optionName;      // 옵션 이름 (예: "양념 추가")
    private int priceAtOrder;       // 주문 시점에 고정된 옵션 가격. 나중에 옵션 값이 바뀌어도 주문 당시 가격을 그대로 보존하기 위함

    /**
     * 엔티티(OrderItemOption) -> 응답 DTO 로 바꿔주는 static 팩토리 메서드.
     * "new" 대신 from(...)을 써서 "엔티티에서 만들어낸다"는 의도를 이름으로 드러낸다(컨벤션).
     */
    public static OrderItemOptionResponse from(OrderItemOption option) {
        return OrderItemOptionResponse.builder()
                .optionId(option.getMenuOption().getId())       // OrderItemOption -> 연결된 MenuOption -> 그 PK 를 꺼냄
                .optionName(option.getMenuOption().getName())   // 옵션 이름도 연결된 MenuOption 에서 가져옴
                .priceAtOrder(option.getPriceAtOrder())         // 가격은 주문 항목에 박제된 값(주문 당시 가격)을 그대로 사용
                .build();
    }
}
