package com.kuit.baemin.domain.menu;

import com.kuit.baemin.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;   // DB 레벨 CHECK 제약 생성용

import java.util.ArrayList;
import java.util.List;

/**
 * 옵션 그룹 (ERD: option_group) — 메뉴에 딸린 선택 항목들의 묶음.
 * 예: "맵기 선택"(필수, 단일), "토핑 추가"(선택, 다중). 이 그룹 안에 실제 보기(MenuOption)들이 들어간다.
 * 테이블 한 행 = OptionGroup 객체 하나. 관계: MENU 1 : N OPTION_GROUP, OPTION_GROUP 1 : N MENU_OPTION
 */
@Entity
@Getter
@Builder
// uniqueConstraints: 한 메뉴(menu_id) 안에서 옵션 그룹 이름(name)이 중복되지 않도록 복합 UNIQUE (업무 규칙 가정 — 깨지면 이 제약 제거)
@Table(name = "option_group",
       uniqueConstraints = @UniqueConstraint(name = "uk_option_group_menu_name", columnNames = {"menu_id", "name"}))
@Check(constraints = "selection_type in ('single','multiple')")   // selection_type 허용값 제한
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 이 옵션 그룹이 속한 메뉴 (N:1) ---
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)  // 이 테이블에 만들 외래키(FK) 컬럼명. nullable=false → 메뉴 없는 옵션 그룹은 불가
    private Menu menu;

    @Column(nullable = false, length = 100)          // NOT NULL, VARCHAR(100)
    private String name;

    @Column(name = "is_required", nullable = false)  // 옵션 선택이 필수인지 여부. boolean → DB 컬럼명은 is_required
    private boolean required;

    // 단일/다중 선택 구분. ERD: selection_type ENUM('single','multiple')
    // @Enumerated 대신 SelectionTypeConverter(autoApply=true)가 자동으로 소문자 문자열 변환을 담당 (SINGLE <-> "single")
    @Column(name = "selection_type", nullable = false, length = 10)
    private SelectionType selectionType;

    // --- 연관관계: 이 그룹에 속한 실제 옵션 보기들 (1:N) ---
    @Builder.Default
    @OneToMany(mappedBy = "optionGroup",             // 1:N의 주인은 MenuOption.optionGroup 쪽(FK 보유). 여기는 읽기 전용 반대편
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<MenuOption> options = new ArrayList<>();

    /** 연관관계 편의 메서드: 옵션을 양쪽(그룹 리스트 + 옵션의 그룹 참조)에 동시에 세팅해 데이터 정합성을 맞춘다 */
    public void addOption(MenuOption option) {
        this.options.add(option);
        option.setOptionGroup(this);
    }
}
