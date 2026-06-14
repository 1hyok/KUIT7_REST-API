package com.kuit.baemin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 등록 요청 DTO.
 * 클라이언트가 보낸 JSON 요청 본문(request body)을 담아 컨트롤러로 전달하는 그릇 역할.
 * 컨트롤러에서 @RequestBody @Valid 로 받으면 아래 검증 어노테이션들이 자동으로 동작해,
 * 값이 규칙에 어긋나면 본문이 서비스 로직에 닿기 전에 예외(400 Bad Request)로 막힌다.
 */
@Getter // Lombok: 모든 필드의 getter 자동 생성. 서비스 등에서 이 DTO의 값을 꺼내 읽을 때 사용 (요청 JSON→객체 변환은 Spring의 Jackson이 담당)
public class MenuCreateRequest {

    @NotBlank(message = "메뉴 이름은 필수입니다.")              // null·빈 문자열·공백만("   ") 모두 불허 (문자열 전용 검증)
    @Size(max = 100, message = "메뉴 이름은 100자 이하여야 합니다.") // 글자 수 상한. 100자까지 허용
    private String name;

    private String description; // 메뉴 설명. 검증 어노테이션 없음 = 선택값(null 허용)

    @NotNull(message = "메뉴 가격은 필수입니다.")        // 값 자체가 반드시 있어야 함(null 불허). 숫자라 @NotBlank가 아닌 @NotNull 사용
    @Positive(message = "메뉴 가격은 1 이상이어야 합니다.") // 0과 음수 불허. 양수만 허용
    private Integer price;

    // --- 연관 입력: 메뉴에 딸린 옵션 그룹들 (예: "맵기 선택", "토핑 추가") ---
    @Valid                                                  // 중첩 검증. 이게 있어야 리스트 각 OptionGroupRequest 내부의 검증 어노테이션까지 함께 작동
    private List<OptionGroupRequest> optionGroups = new ArrayList<>(); // 빈 리스트로 초기화 = 옵션을 안 보내도 null이 아니라서 NPE 방지
}
