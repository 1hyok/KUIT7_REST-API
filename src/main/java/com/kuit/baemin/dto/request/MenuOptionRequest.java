package com.kuit.baemin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 메뉴 옵션 1개를 등록할 때 클라이언트가 보내는 요청 데이터(DTO).
 * <p>
 * 요청(request) 계층의 객체로, 컨트롤러가 받은 JSON을 이 자바 객체로 변환(역직렬화)한다.
 * 단독으로 쓰이기보다 {@link OptionGroupRequest} 안의 옵션 목록(List)의 한 원소로 들어간다.
 * (예: "맵기" 옵션그룹 → "순한맛 / 보통맛 / 매운맛" 같은 개별 옵션 하나가 MenuOptionRequest 하나)
 */
@Getter   // Lombok: 모든 필드의 getter 자동 생성 (JSON -> 객체 변환 및 서비스에서 값 꺼낼 때 사용)
public class MenuOptionRequest {

    @NotBlank(message = "옵션 이름은 필수입니다.")          // null + 빈 문자열 + 공백("  ")까지 모두 거부 (문자열 전용)
    @Size(max = 100, message = "옵션 이름은 100자 이하여야 합니다.")  // 글자 수 상한 검사 (최대 100자)
    private String name;

    @PositiveOrZero(message = "옵션 추가 금액은 0 이상이어야 합니다.")  // 0 또는 양수만 허용 (음수 거부). 무료 옵션은 0원으로 표현
    private int extraPrice;
}
