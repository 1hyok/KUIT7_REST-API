package com.kuit.baemin.domain.order;

import com.kuit.baemin.domain.BaseEntity;
import com.kuit.baemin.domain.menu.Menu;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 항목 (ERD: order_item) — ORDERS 1 : N ORDER_ITEM, MENU 1 : N ORDER_ITEM
 */
@Entity
@Getter
@Builder
@Table(name = "order_item")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 어떤 주문(Order)의, 어떤 메뉴(Menu) 항목인지 ---
    @Setter                                        // (Lombok) 이 필드의 세터 setOrder() 자동 생성. 연관관계 편의 메서드(Order.addOrderItem)가 양방향 연결 시 order 를 채우려고 둠
    @ManyToOne(fetch = FetchType.LAZY)             // N:1 관계(주문 1개에 항목 여러 개). LAZY = order를 실제로 꺼내 쓸 때만 DB 조회
    @JoinColumn(name = "order_id", nullable = false)   // 이 테이블에 만들 외래키(FK) 컬럼명. NOT NULL
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)             // N:1 관계(메뉴 1개가 여러 주문 항목에 담길 수 있음). LAZY 조회
    @JoinColumn(name = "menu_id", nullable = false)    // menu 테이블을 가리키는 FK 컬럼. NOT NULL
    private Menu menu;

    @Column(nullable = false)
    private int quantity;                          // 주문 수량

    /** 주문 시점 메뉴 단가 스냅샷 (나중에 메뉴 가격이 바뀌어도 과거 주문 금액은 그대로 보존) */
    @Column(name = "price_at_order", nullable = false)
    private int priceAtOrder;

    // --- 연관관계: 이 항목에 선택된 옵션들(예: 사이즈 Large, 토핑 추가) ---
    @Builder.Default
    @OneToMany(mappedBy = "orderItem",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<OrderItemOption> options = new ArrayList<>();

    /**
     * 연관관계 편의 메서드 — 옵션을 추가하면서 양방향 연결(부모↔자식)을 한 번에 맞춰줌.
     * 한쪽만 set 하면 객체 그래프가 어긋날 수 있어 둘 다 채운다.
     */
    public void addOption(OrderItemOption option) {
        this.options.add(option);                  // 부모(this) → 자식(option) 방향
        option.setOrderItem(this);                 // 자식(option) → 부모(this) 방향(FK 주인 쪽)
    }

    /** 항목 소계 = (메뉴 단가 + 옵션 추가금 합) × 수량 */
    public int getSubtotal() {
        int optionTotal = options.stream()                     // 선택 옵션들을 순회하며
                .mapToInt(OrderItemOption::getPriceAtOrder)    // 인자 = "옵션 1개 → int" 변환 규칙(o -> o.getPriceAtOrder()). mapToInt 가 모든 옵션에 한 번씩 적용 → int 들의 흐름(IntStream) → 아래 sum()
                .sum();                                        // 모두 더함 = 옵션 추가금 합
        return (priceAtOrder + optionTotal) * quantity;
    }
}
