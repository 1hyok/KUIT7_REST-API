package com.kuit.baemin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

/**
 * 주문 생성 요청 DTO.
 * 클라이언트가 보낸 주문 요청 JSON을 담는 그릇으로, 컨트롤러의 @RequestBody로 받아 사용한다.
 * 엔티티(Order)와 분리해 "외부에서 받는 입력"과 "DB에 저장되는 형태"의 책임을 나눈다.
 * 컨트롤러에서 @Valid를 붙이면 아래 검증 어노테이션들이 동작해, 값이 비면 400 응답으로 막아준다.
 */
@Getter                                              // Lombok이 각 필드의 getter를 생성. 서비스 등에서 값을 꺼내 읽을 때 사용 (요청 JSON→객체 변환은 Spring의 Jackson이 담당)
public class OrderCreateRequest {

    // 인증 미구현 단계라 주문자 memberId를 요청 본문으로 받음 (8주차에서 토큰 기반으로 대체)
    @NotNull(message = "회원 ID는 필수입니다.")          // null이면 검증 실패 (값 자체가 있어야 함)
    private Long memberId;

    @NotNull(message = "가게 ID는 필수입니다.")
    private Long restaurantId;

    @NotNull(message = "배송지 ID는 필수입니다.")
    private Long addressId;

    @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")  // null도 빈 리스트도 안 됨 (항목이 최소 1개는 있어야 함)
    @Valid                                               // 리스트 안의 각 OrderItemRequest까지 재귀로 검증 (중첩 검증)
    private List<OrderItemRequest> items;
}
