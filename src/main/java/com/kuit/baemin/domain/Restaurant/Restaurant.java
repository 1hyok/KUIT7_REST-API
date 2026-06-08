package com.kuit.baemin.domain.Restaurant;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.BaseEntity;
import com.kuit.baemin.domain.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 가게(Restaurant) 엔티티.
 * "엔티티"란 DB 테이블과 1:1로 연결되는 자바 클래스입니다.
 * → restaurant 테이블의 한 행(row) = 이 클래스의 객체 하나.
 * (ERD 관계: CATEGORY 1 : N RESTAURANT — 카테고리 하나에 가게가 여러 개)
 */
@Entity
@Getter                                              // (Lombok) 모든 필드의 조회 메서드(getName() 등)를 자동 생성해 줌
@Builder                                             // (Lombok) Restaurant.builder().name("..").build() 형태로 객체를 만들게 해줌
@Table(name = "restaurant")                          // 매핑할 실제 DB 테이블 이름 (ERD에 맞춰 단수)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {         // BaseEntity 상속 → createdAt/updatedAt(생성·수정 시각) 필드를 물려받음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 여러 가게(N)가 한 카테고리(1)에 속함 ---
    @ManyToOne(fetch = FetchType.LAZY)                       // N:1 관계. LAZY = category를 '실제로 꺼내 쓸 때'만 DB 조회 (불필요한 조회를 줄이는 성능 설정)
    @JoinColumn(name = "category_id", nullable = false)      // 이 테이블에 만들 외래키(FK) 컬럼명 = category_id, 반드시 값 있어야 함
    private Category category;

    @Column(nullable = false, length = 100)   // DB 컬럼 규칙: NOT NULL(필수), 최대 길이 100자
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(precision = 10, scale = 7)        // 위도/경도 같은 소수: 전체 10자리 중 소수점 아래 7자리
    private BigDecimal latitude;              // BigDecimal = 오차 없는 정밀 소수 타입 (좌표·금액에 사용)

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "min_order_price", nullable = false)   // 자바 필드는 camelCase, DB 컬럼은 snake_case로 이름을 따로 지정
    private int minOrderPrice;                            // 최소 주문 금액

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;                              // 배달비

    // ERD: status ENUM('active','inactive') — ActiveStatusConverter 가 소문자로 매핑 (@Enumerated 대신 컨버터 사용)
    @Column(nullable = false, length = 10)
    private ActiveStatus status;

    /** 이 가게가 영업(활성) 상태인지 확인하는 편의 메서드 (주문 가능 여부 판단에 사용) */
    public boolean isActive() {
        return this.status == ActiveStatus.ACTIVE;
    }
}
