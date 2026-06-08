package com.kuit.baemin.domain.menu;

import com.kuit.baemin.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 메뉴 옵션 (ERD: menu_option) — OPTION_GROUP 1 : N MENU_OPTION
 *
 * <p>menu_option 테이블의 한 행 = 이 객체 하나. (예: "곱빼기 +1,000원", "맵게 +0원")
 * 하나의 옵션 그룹(OptionGroup, 예: "면 양 선택")에 여러 개의 선택지(MenuOption)가 묶인다.
 * 생성/수정 시각 등 공통 컬럼은 BaseEntity 를 상속받아 물려받는다.
 */
@Entity
@Getter
@Builder
@Table(name = "menu_option")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 어떤 옵션 그룹(OptionGroup)에 속한 옵션인지 ---
    @Setter                                              // 이 FK 필드만 외부에서 교체 가능 (양방향 연관관계 세팅 등에 사용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false) // 이 테이블에 만들 외래키(FK) 컬럼명. nullable=false → 옵션은 반드시 그룹에 소속
    private OptionGroup optionGroup;                    // 자바에선 OptionGroup '객체' 자체(그룹 전체 접근 가능). DB엔 그 객체의 id만 위 option_group_id 컬럼에 저장됨

    @Column(nullable = false, length = 100)              // NOT NULL, 최대 길이 100인 VARCHAR 컬럼
    private String name;

    @Column(name = "extra_price", nullable = false)      // 추가 금액 컬럼. 이 옵션을 고르면 더 붙는 가격 (0이면 무료 옵션)
    private int extraPrice;
}
