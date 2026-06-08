package com.kuit.baemin.domain.menu;

/**
 * 옵션 그룹(OptionGroup)의 선택 방식을 나타내는 enum.
 * 예) "맵기 선택" 그룹은 하나만 고르는 SINGLE, "추가 토핑" 그룹은 여러 개 고르는 MULTIPLE.
 * (ERD: option_group.selection_type ENUM('single','multiple'))
 */
public enum SelectionType {
    SINGLE,    // 단일 선택
    MULTIPLE   // 다중 선택
}
