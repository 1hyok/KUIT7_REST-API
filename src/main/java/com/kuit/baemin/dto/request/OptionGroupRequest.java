package com.kuit.baemin.dto.request;

import com.kuit.baemin.domain.menu.SelectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 생성 요청(MenuCreateRequest) 안에 들어가는 "옵션 그룹" 입력용 DTO.
 * 클라이언트(앱/프론트)가 보낸 JSON을 받아 담는 요청 객체이며, DB 엔티티가 아니다.
 * 예) "맵기 선택"(하나만 고르기) 같은 묶음 하나가 OptionGroupRequest 하나에 해당.
 * 한 옵션 그룹은 그 안에 여러 개의 옵션(MenuOptionRequest)을 가진다. (옵션 그룹 1 : 옵션 N)
 */
@Getter
public class OptionGroupRequest {

    @NotBlank(message = "옵션 그룹 이름은 필수입니다.")          // null/빈 문자열/공백만 입력 금지 (문자열 전용 검증)
    @Size(max = 100, message = "옵션 그룹 이름은 100자 이하여야 합니다.")  // 글자 수 상한 제한
    private String name;

    private boolean required;   // 이 옵션 그룹을 손님이 반드시 골라야 하는지 여부 (필수 옵션 / 선택 옵션)

    @NotNull(message = "선택 방식(SINGLE/MULTIPLE)은 필수입니다.")  // 값이 반드시 있어야 함. enum 이라 @NotBlank 가 아닌 @NotNull 사용
    private SelectionType selectionType;   // 단일 선택(SINGLE) / 다중 선택(MULTIPLE) 중 하나. (enum 정의: SelectionType)

    // --- 하위 옵션 목록 ---
    @Valid   // 중첩 검증: 리스트 안의 MenuOptionRequest 각각도 @NotBlank 등 검증 규칙을 함께 적용 (안 붙이면 내부는 검사 안 됨)
    private List<MenuOptionRequest> options = new ArrayList<>();   // 빈 리스트로 초기화 → 요청에 options 가 없어도 null 이 아님 (NPE 예방)
}
