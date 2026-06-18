package com.kuit.baemin.domain.menu;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.BaseEntity;
import com.kuit.baemin.domain.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;   // DB 레벨 CHECK 제약 생성용

import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 (ERD: menu) — RESTAURANT 1 : N MENU, MENU 1 : N OPTION_GROUP
 *
 * <p>이 엔티티 객체 하나가 menu 테이블의 한 행(row)과 대응된다.
 * 한 식당(Restaurant)은 여러 메뉴를 가지고, 한 메뉴는 여러 옵션 그룹(OptionGroup)을 가진다.
 * 생성/수정 시각 같은 공통 컬럼은 BaseEntity 를 상속받아 재사용한다.
 */
@Entity
@Getter
@Builder
@Table(name = "menu")
@Check(constraints = "status in ('active','inactive')")   // status 컬럼 허용값 제한
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 이 메뉴가 속한 식당 (N:1) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false) // 이 menu 테이블에 만들 외래키(FK) 컬럼명. NOT NULL → 메뉴는 반드시 식당에 소속
    private Restaurant restaurant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")   // DB 컬럼 타입을 직접 TEXT로 지정 — VARCHAR 길이 제한 없이 긴 설명을 담기 위함
    private String description;

    @Column(nullable = false)
    private int price;

    // ERD: menu.status ENUM('active','inactive') — ActiveStatusConverter(autoApply=true) 가 자동으로 소문자 매핑
    // 그래서 @Enumerated 를 붙이지 않는다 (Converter 와 @Enumerated 는 함께 쓸 수 없음)
    @Column(nullable = false, length = 10)
    private ActiveStatus status;

    // --- 연관관계: 이 메뉴에 딸린 옵션 그룹들 (1:N) ---
    @Builder.Default                                     // @Builder 로 생성해도 이 필드는 빈 리스트로 초기화 (null 방지)
    @OneToMany(mappedBy = "menu",                        // mappedBy → FK는 OptionGroup 쪽 menu 필드가 관리. 이 List는 "거울"일 뿐 FK 컬럼을 만들지 않음
               cascade = CascadeType.ALL,               // 메뉴를 저장/삭제하면 소속 옵션 그룹도 함께 저장/삭제
               orphanRemoval = true)                    // 이 리스트에서 빠진 옵션 그룹은 DB에서도 삭제 (고아 객체 제거)
    private List<OptionGroup> optionGroups = new ArrayList<>();

    /**
     * 연관관계 편의 메서드.
     * 양방향 연관관계에서 한쪽만 세팅하면 다른 쪽이 비어 객체 상태가 어긋나므로,
     * 리스트 추가와 반대편(OptionGroup.menu) 세팅을 한 번에 처리해 양쪽을 항상 일치시킨다.
     */
    public void addOptionGroup(OptionGroup optionGroup) {
        this.optionGroups.add(optionGroup);
        optionGroup.setMenu(this);
    }

    /** 주문 가능 여부: 상태가 ACTIVE 일 때만 true (INACTIVE = 숨김/판매중지) */
    public boolean isOrderable() {
        return this.status == ActiveStatus.ACTIVE;
    }
}
