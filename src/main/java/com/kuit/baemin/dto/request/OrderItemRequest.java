package com.kuit.baemin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 1건에 담기는 "메뉴 한 줄"을 담는 요청 DTO.
 * 클라이언트가 보낸 JSON 한 덩어리(어떤 메뉴를, 몇 개, 어떤 옵션으로)를 받는 그릇으로,
 * 보통 OrderCreateRequest 안에 List<OrderItemRequest> 형태로 여러 개 들어간다. (장바구니의 한 줄 = 이 객체 하나)
 * 컨트롤러에서 @Valid 와 함께 받으면 아래 검증 어노테이션이 자동으로 동작한다.
 */
@Getter
public class OrderItemRequest {

    @NotNull(message = "메뉴 ID는 필수입니다.")   // 어떤 메뉴인지 식별하는 값. null이면 검증 실패 (message가 에러 메시지로 나감)
    private Long menuId;

    @NotNull(message = "수량은 필수입니다.")        // 값 자체가 비어 있으면(null) 실패
    @Positive(message = "수량은 1 이상이어야 합니다.")  // 0 이하(0, 음수)면 실패. 즉 1개 이상만 허용
    private Integer quantity;

    /** 선택한 옵션 ID 목록 (없으면 빈 리스트) */
    private List<Long> optionIds = new ArrayList<>();
}
