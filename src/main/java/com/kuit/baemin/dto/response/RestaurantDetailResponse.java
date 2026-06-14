package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.menu.Menu;
import com.kuit.baemin.domain.restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 가게 상세 '응답 DTO'.
 * 가게 1개의 기본 정보 + 그 가게가 파는 메뉴 목록(menus)까지 한 번에 담아 내보냅니다.
 * (목록용 RestaurantResponse와 달리, 메뉴 리스트와 위/경도까지 포함하는 '상세 화면'용)
 * 엔티티를 그대로 응답하지 않고 화면에 필요한 필드만 골라 담는 이유는 RestaurantResponse 참고.
 */
@Getter
@Builder
public class RestaurantDetailResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String phone;
    private String address;
    private BigDecimal latitude;     // 위도. 좌표는 소수점 오차가 없어야 해서 BigDecimal 사용
    private BigDecimal longitude;    // 경도
    private int minOrderPrice;
    private int deliveryFee;
    private List<MenuResponse> menus;     // 이 가게의 메뉴 목록 (각 메뉴도 응답 DTO로 변환된 형태)

    /**
     * 엔티티 → 상세 응답 DTO 변환 (정적 팩토리 메서드).
     * 가게 1개와 메뉴 목록 '두 개'를 합쳐 만들기 때문에 from이 아니라 of 라는 이름을 씁니다.
     * (입력이 1개면 보통 from, 2개 이상을 조합하면 of 라는 관례)
     * 'static' 이라 객체를 만들지 않고 RestaurantDetailResponse.of(가게, 메뉴목록) 형태로 바로 호출합니다.
     */
    public static RestaurantDetailResponse of(Restaurant restaurant, List<Menu> menus) {
        return RestaurantDetailResponse.builder()
                .id(restaurant.getId())
                .categoryId(restaurant.getCategory().getId())       // 연관된 카테고리에서 id를 꺼냄 (LAZY라 이 순간 DB 조회 발생)
                .categoryName(restaurant.getCategory().getName())   // 카테고리 이름도 함께
                .name(restaurant.getName())
                .phone(restaurant.getPhone())
                .address(restaurant.getAddress())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .minOrderPrice(restaurant.getMinOrderPrice())
                .deliveryFee(restaurant.getDeliveryFee())
                .menus(menus.stream()                               // 메뉴 엔티티 리스트를
                        .map(MenuResponse::from)                         // 한 개씩 MenuResponse(응답 DTO)로 변환하고
                        .toList())                                  // 다시 리스트로 모음 (List<Menu> → List<MenuResponse>)
                .build();                                           // 조립 완료 → RestaurantDetailResponse 객체 반환
    }
}
