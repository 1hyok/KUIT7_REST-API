package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.menu.OptionGroup;
import com.kuit.baemin.domain.menu.SelectionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 옵션 그룹 "응답" DTO.
 * 클라이언트(앱/브라우저)에게 내려줄 데이터만 담는 그릇이다.
 * OptionGroup 엔티티를 그대로 노출하지 않고, 이 DTO로 필요한 값만 골라 응답한다.
 * (엔티티를 직접 응답하면 연관관계 지연로딩·민감정보 노출 등 문제가 생길 수 있어 DTO로 분리)
 * 한 옵션 그룹은 여러 개의 옵션(MenuOption)을 가지므로, 그 보기들도 MenuOptionResponse 리스트로 함께 담아 내려준다.
 */
@Getter   // Lombok이 각 필드의 getter를 자동 생성 (JSON 직렬화 시 필요)
@Builder  // Lombok이 빌더 패턴(.builder().id(..)...build())을 자동 생성
public class OptionGroupResponse {

    private Long id;
    private String name;
    private boolean required;             // 이 그룹에서 옵션 선택이 필수인지 여부
    private SelectionType selectionType;  // 단일(SINGLE)/다중(MULTIPLE) 선택 구분 enum
    private List<MenuOptionResponse> options;  // 이 그룹에 속한 실제 옵션 보기들 (역시 DTO로 변환해서 담음)

    // 엔티티(OptionGroup) -> 응답 DTO(OptionGroupResponse) 변환용 static 팩토리 메서드.
    // 'from'은 "다른 타입 하나를 받아 이 타입으로 만든다"는 관례적 이름.
    public static OptionGroupResponse from(OptionGroup group) {
        return OptionGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .required(group.isRequired())              // boolean 필드라 getter 이름이 isRequired()
                .selectionType(group.getSelectionType())
                // 자식 엔티티 리스트(List<MenuOption>)를 자식 DTO 리스트(List<MenuOptionResponse>)로 한 건씩 변환.
                // map(MenuOptionResponse::from) = 각 MenuOption에 MenuOptionResponse.from()을 적용, toList()로 다시 리스트로 모음.
                .options(group.getOptions().stream()
                        .map(MenuOptionResponse::from)
                        .toList())
                .build();
    }
}
