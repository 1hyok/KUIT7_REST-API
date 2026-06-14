package com.kuit.baemin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 가게 등록 '요청 DTO'.
 * DTO(Data Transfer Object) = 계층 사이에 데이터를 실어 나르는 그릇.
 * 클라이언트가 보낸 JSON이 이 객체로 변환되고, 아래 어노테이션들로 값이 올바른지 자동 검사됩니다.
 * (엔티티를 직접 요청으로 받지 않고 DTO를 따로 두는 이유: 외부 입력과 DB 모델을 분리해 안전하게 다루기 위함)
 */
@Getter   // (Lombok) getCategoryId() 같은 조회 메서드 자동 생성 → 스프링이 JSON을 이 객체에 채울 때 사용
public class RestaurantCreateRequest {

    @NotNull(message = "카테고리 ID는 필수입니다.")   // 값이 null이면 검증 실패 (숫자/객체의 '빈 값' 검사)
    private Long categoryId;

    @NotBlank(message = "가게 이름은 필수입니다.")        // 문자열이 null·"" ·공백뿐이면 실패 (문자열 전용)
    @Size(max = 100, message = "가게 이름은 100자 이하여야 합니다.")  // 길이 제한
    private String name;

    @NotBlank(message = "가게 전화번호는 필수입니다.")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;

    @NotBlank(message = "가게 주소는 필수입니다.")
    @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
    private String address;

    private BigDecimal latitude;    // 위도(선택값) — 검증 어노테이션이 없으니 없어도 통과

    private BigDecimal longitude;   // 경도(선택값)

    @PositiveOrZero(message = "최소 주문 금액은 0 이상이어야 합니다.")   // 0 또는 양수만 허용 (음수 차단)
    private int minOrderPrice;

    @PositiveOrZero(message = "배달비는 0 이상이어야 합니다.")
    private int deliveryFee;
    // ↑ 이 검증들은 컨트롤러에서 @Valid가 붙어 있을 때 자동 실행됩니다. 위반하면 400(잘못된 요청) 에러가 납니다.
}
