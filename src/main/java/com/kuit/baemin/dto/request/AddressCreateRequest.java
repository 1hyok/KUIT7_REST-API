package com.kuit.baemin.dto.request;

import com.kuit.baemin.domain.address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 배송지(주소) 등록 요청 DTO.
 *
 * <p>클라이언트가 보낸 JSON 요청 본문(body)을 받아 담는 그릇이다.
 * 컨트롤러에서 @RequestBody 로 이 객체에 매핑되며, @Valid 와 함께 쓰면
 * 아래 Bean Validation(@NotNull/@NotBlank/@Size) 검증이 컨트롤러 진입 시 자동 수행된다.
 * (엔티티와 분리된 "입력 전용" 객체라서, DB 테이블과 1:1로 매핑되지 않는다.)
 *
 * <p>@Getter 만 두고 @Setter 는 두지 않는다. 요청 값은 JSON -> 객체 변환 시 한 번만 채워지면 되고,
 * 이후 코드에서 값을 바꾸지 못하게 막아 안전하게 다루기 위함이다.
 */
@Getter
public class AddressCreateRequest {

    @NotNull(message = "배송지 유형(HOME/WORK/ETC)은 필수입니다.")   // null 이면 검증 실패. enum 타입이라 @NotBlank(문자열 전용) 대신 @NotNull 사용
    private AddressType type;

    @Size(max = 50, message = "별칭은 50자 이하여야 합니다.")        // 값이 있을 때만 길이 검사. 값 자체가 없어도(null) 통과 -> 즉 선택 입력 필드
    private String alias;

    @NotBlank(message = "도로명 주소는 필수입니다.")                 // null/빈문자열/공백("   ")까지 모두 거름. 문자열 필수 입력에 사용
    @Size(max = 255, message = "도로명 주소는 255자 이하여야 합니다.") // 동시에 길이도 제한 (검증 여러 개를 한 필드에 겹쳐 적용 가능)
    private String roadAddress;

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")  // 선택 입력 + 길이 제한만
    private String detailAddress;

    // 위도/경도. 좌표는 소수점 오차가 없어야 하므로 double 대신 BigDecimal 사용
    private BigDecimal latitude;   // 위도

    private BigDecimal longitude;  // 경도
}
