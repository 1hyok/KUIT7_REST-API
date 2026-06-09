package com.kuit.baemin.domain.order;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.BaseEntity;
import com.kuit.baemin.domain.restaurant.Restaurant;
import com.kuit.baemin.domain.address.Address;
import com.kuit.baemin.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 엔티티 (ERD: orders 테이블). 테이블의 한 행(row) = 이 객체 하나.
 *
 * <p>한 건의 주문이 "누가(member) / 어느 가게에서(restaurant) / 어디로 배달(address)"를
 * 가리키며, 무엇을 주문했는지는 orderItems 로 펼쳐진다.
 * 관계: USER 1:N ORDERS, RESTAURANT 1:N ORDERS, ADDRESS 1:N ORDERS, ORDERS 1:N ORDER_ITEM.
 */
@Entity
@Getter
@Builder                                             // (Lombok) Order.builder()...build() 형태로 객체 생성. 아래 전체 인자 생성자에 위임한다
@Table(name = "orders")                              // 매핑할 테이블명. order 는 SQL 예약어라 복수형 orders 사용
@AllArgsConstructor                                  // (Lombok) 모든 필드를 받는 생성자. @Builder 가 이걸로 객체를 채운다(@NoArgsConstructor 가 있으면 자동생성이 막혀 명시 필요)
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // (Lombok) JPA 가 요구하는 무인자 생성자. protected 로 막아 외부의 new Order() 를 차단 → 빌더 사용 유도
public class Order extends BaseEntity {              // BaseEntity 상속 → 생성/수정 시각 등 공통 컬럼을 물려받음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 이 주문이 가리키는 대상들 (모두 N:1) ---
    @ManyToOne(fetch = FetchType.LAZY)               // N:1 관계(주문 여러 건 → 회원 1명). LAZY = 실제로 member 를 꺼내 쓸 때만 DB 조회
    @JoinColumn(name = "user_id", nullable = false)  // 이 테이블에 만들 외래키(FK) 컬럼명 (ERD: orders.user_id), null 불가
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)               // N:1 관계(주문 여러 건 → 식당 1개)
    @JoinColumn(name = "restaurant_id", nullable = false)  // FK 컬럼 restaurant_id
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)               // N:1 관계(주문 여러 건 → 배달 주소 1개)
    @JoinColumn(name = "address_id", nullable = false)  // FK 컬럼 address_id
    private Address address;

    /** 총 결제 금액 = 메뉴/옵션 합계 + 배달비 */
    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    /** 주문 시점의 배달비(스냅샷). 가게가 나중에 배달비를 바꿔도 이 주문의 금액 내역은 보존됨 (priceAtOrder 와 같은 개념) */
    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    // ERD: order_status ENUM(...) — OrderStatusConverter(autoApply=true) 가 enum↔소문자 문자열을 자동 변환
    // (Converter 가 처리하므로 여기에 @Enumerated 를 붙이지 않는다)
    // name="order_status": 매핑될 DB 컬럼명(생략 시 필드명 orderStatus 사용)
    // nullable=false: 이 컬럼에 NULL 금지 → DDL 자동생성 시 NOT NULL 제약, 즉 항상 값이 있어야 함
    // length=15: 문자열 컬럼(VARCHAR)의 최대 길이를 15자로 지정(생략 시 기본 255). enum 이름이 문자열로 저장되므로 길이 제한
    // ※ name 은 단순 문자열이라 오타가 나도 컴파일/JPA 가 못 잡는다. 언제 터지는지는 ddl-auto 설정에 달림:
    //    - none(이 프로젝트 운영/로컬): 시작은 통과 → 이 컬럼 쓰는 첫 쿼리에서 DB가 "Unknown column"(MySQL 1054) 런타임 에러
    //    - validate: 시작 시점에 SchemaManagementException("missing column ...")으로 부팅 자체가 막힘(fail-fast)
    //    - create-drop(이 프로젝트 테스트 H2): 오타난 이름 그대로 컬럼을 만들어 매핑과 일치 → 에러 없이 조용히 통과(가장 위험)
    //    결론: DB 실제 컬럼명과 글자 단위로 정확히 일치시켜야 안전
    @Column(name = "order_status", nullable = false, length = 15)
    private OrderStatus orderStatus;

    // 소프트 삭제용 상태(active/inactive). ActiveStatusConverter(autoApply=true) 가 자동 매핑 → @Enumerated 불필요
    @Column(nullable = false, length = 10)
    private ActiveStatus status;

    // --- 연관관계: 이 주문에 포함된 주문 항목들 (1:N, 주인은 OrderItem.order 쪽) ---
    @Builder.Default
    @OneToMany(mappedBy = "order",                   // 1:N. mappedBy = "order" → FK 관리 주인은 OrderItem 의 order 필드 (여기는 읽기용)
            cascade = CascadeType.ALL,               // 주문 저장/삭제 시 하위 OrderItem 들도 함께 저장/삭제(영속성 전이)
            orphanRemoval = true)                    // 이 리스트에서 빠진 OrderItem 은 고아로 보고 DB 에서도 삭제
    private List<OrderItem> orderItems = new ArrayList<>();

    /** 연관관계 편의 메서드: 양쪽(부모 리스트 + 자식의 order)을 동시에 맞춰 데이터 정합성을 유지 */
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);              // 부모(this)의 리스트에 자식 추가
        orderItem.setOrder(this);                    // 자식의 order 필드에도 부모를 세팅 (FK 주인 쪽)
    }

    /** 이 주문의 주인이 memberId 인지 확인 (본인 주문만 조회/취소하도록 권한 체크에 사용) */
    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    /** 주문 취소: 상태를 CANCELED 로 변경 */
    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }

    /** 진행 상태 변경 (예: PENDING → ACCEPTED → COOKING). enum 상태 전이에 사용 */
    public void changeStatus(OrderStatus next) {
        this.orderStatus = next;
    }
}
