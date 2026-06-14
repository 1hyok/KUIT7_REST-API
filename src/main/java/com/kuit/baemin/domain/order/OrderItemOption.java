package com.kuit.baemin.domain.order;

import com.kuit.baemin.domain.BaseEntity;
import com.kuit.baemin.domain.menu.MenuOption;
import jakarta.persistence.*;
import lombok.*;

/**
 * 주문 항목별 선택 옵션 (ERD: order_item_option)
 * ORDER_ITEM 1 : N ORDER_ITEM_OPTION, MENU_OPTION 1 : N ORDER_ITEM_OPTION
 *
 * <p>order_item_option 테이블의 한 행 = 이 객체 하나.
 * "어떤 주문 항목(OrderItem)에서 어떤 옵션(MenuOption)을 골랐는가"를 한 줄로 기록한다.
 * (예: 짜장면 주문 항목에 "곱빼기 +1,000원" 옵션을 추가한 사실)
 * 생성/수정 시각 같은 공통 컬럼은 BaseEntity 를 상속받아 물려받는다.
 */
@Entity
@Getter
@Builder
@Table(name = "order_item_option")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 이 옵션이 속한 주문 항목 (N:1) ---
    @Setter                                              // 이 FK 필드만 외부에서 교체 가능 (OrderItem.addOption 에서 양방향 연관관계를 맺을 때 사용)
    @ManyToOne(fetch = FetchType.LAZY)                   // N:1 관계. LAZY = orderItem을 실제로 꺼내 쓸 때만 DB 조회 (불필요한 조회를 줄임)
    @JoinColumn(name = "order_item_id", nullable = false) // 이 테이블에 만들 외래키(FK) 컬럼명. NOT NULL → 옵션은 반드시 어떤 주문 항목에 소속
    private OrderItem orderItem;

    // --- 연관관계: 손님이 실제로 고른 메뉴 옵션 (N:1) ---
    @ManyToOne(fetch = FetchType.LAZY)                   // N:1 관계. LAZY = menuOption을 실제로 꺼내 쓸 때만 DB 조회
    @JoinColumn(name = "option_id", nullable = false)    // 이 테이블에 만들 외래키(FK) 컬럼명. NOT NULL → 어떤 옵션을 골랐는지 반드시 기록
    private MenuOption menuOption;

    /**
     * 주문 시점 옵션 추가 금액 스냅샷.
     * 메뉴 옵션(MenuOption)의 가격은 나중에 바뀔 수 있으므로, 주문이 들어온 그 순간의 값을 따로 복사해 둔다.
     * (가격이 바뀌어도 과거 주문 금액은 그대로 보존됨)
     */
    @Column(name = "price_at_order", nullable = false)
    private int priceAtOrder;

    /**
     * 정적 팩토리 메서드: 고른 메뉴 옵션(MenuOption)으로 주문 옵션 한 줄을 만든다.
     * 이때 옵션의 현재 추가금(extraPrice)을 priceAtOrder 로 복사해 "주문 시점 가격"을 고정한다.
     * (orderItem 은 아직 비어 있고, 나중에 OrderItem.addOption 으로 연결된다)
     */
    public static OrderItemOption of(MenuOption menuOption) {
        return OrderItemOption.builder()
                .menuOption(menuOption)
                .priceAtOrder(menuOption.getExtraPrice())  // 주문 그 순간의 옵션 추가금을 스냅샷으로 저장
                .build();
    }
}
