package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

/**
 * 가게 목록 '응답 DTO'.
 * 엔티티(Restaurant)를 그대로 응답하지 않고, 클라이언트에게 보여줄 필드만 골라 담아 내보냅니다.
 * (엔티티를 직접 노출하면 비밀번호 등 민감 정보까지 새어 나가거나, 불필요한 연관 조회가 일어날 수 있어 위험)
 */
@Getter
@Builder
public class RestaurantResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String phone;
    private String address;
    private int minOrderPrice;
    private int deliveryFee;

    /**
     * 엔티티 → 응답 DTO 변환 (정적 팩토리 메서드).
     * 'static' 이라 객체를 만들지 않고 RestaurantResponse.from(가게) 형태로 바로 호출합니다.
     * Service가 DB에서 꺼낸 Restaurant를 이 메서드로 화면용 데이터로 바꿔 줍니다.
     */
    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .categoryId(restaurant.getCategory().getId())       // 연관된 카테고리에서 id를 꺼냄 (LAZY라 이 순간 DB 조회 발생)
                .categoryName(restaurant.getCategory().getName())   // 카테고리 이름도 함께
                .name(restaurant.getName())
                .phone(restaurant.getPhone())
                .address(restaurant.getAddress())
                .minOrderPrice(restaurant.getMinOrderPrice())
                .deliveryFee(restaurant.getDeliveryFee())
                .build();                                           // 조립 완료 → RestaurantResponse 객체 반환
    }
}
