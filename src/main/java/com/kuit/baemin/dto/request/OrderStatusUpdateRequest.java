package com.kuit.baemin.dto.request;

import com.kuit.baemin.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 주문 상태 변경 요청 DTO.
 *
 * <p>클라이언트가 보낸 JSON 요청 본문(body)을 담는 그릇이다.
 * 컨트롤러에서 @RequestBody 로 이 객체에 매핑되며, @Valid 와 함께 쓰면
 * 아래 Bean Validation(@NotNull) 검증이 컨트롤러 진입 전에 자동 수행된다.
 * (예: {"status": "COOKING"} 같은 JSON 이 들어오면 status 필드에 OrderStatus.COOKING 으로 채워진다.)
 *
 * <p>@Getter 만 두고 @Setter 는 두지 않는다. 요청 값은 JSON -> 객체 변환 시 한 번만 채워지면 되고,
 * 이후 코드에서 값을 바꾸지 못하게 막아 안전하게 다루기 위함이다.
 */
@Getter
public class OrderStatusUpdateRequest {

    @NotNull(message = "변경할 주문 상태는 필수입니다.")  // null 이면 검증 실패. enum 타입이라 @NotBlank(문자열 전용) 대신 @NotNull 사용
    private OrderStatus status;                        // 변경할 목표 상태 (OrderStatus enum 의 6가지 값 중 하나)
}
