package com.kuit.baemin.domain.category;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 음식 카테고리 (ERD: category) — 예: "치킨", "한식", "분식".
 *
 * <p>JPA 엔티티는 "DB 테이블 한 행(row) = 자바 객체 하나"로 대응된다.
 * 즉 category 테이블의 한 줄이 이 Category 인스턴스 하나가 된다.
 * 생성/수정 시각(createdAt, updatedAt)은 부모 {@link BaseEntity} 에서 공통으로 물려받는다.
 */
@Entity
@Getter                                              // 모든 필드의 getter 자동 생성 (setter 는 안 만들어 외부에서 막 못 바꾸게 함)
@Builder                                             // 빌더 패턴 생성자 자동 생성 (Category.builder().name(..).build() 형태)
@Table(name = "category")                            // 매핑할 실제 테이블명 지정 (안 쓰면 클래스명 기반 기본값)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)  // NOT NULL + VARCHAR(50) + UNIQUE(같은 이름의 카테고리 중복 불가)
    private String name;

    // ERD: category.status ENUM('active','inactive') — ActiveStatusConverter(autoApply=true) 가 자동으로
    // 자바 enum ↔ 소문자 문자열 변환을 처리한다. (ACTIVE -> "active") 그래서 여기엔 @Enumerated 를 붙이지 않는다.
    @Column(nullable = false, length = 10)
    private ActiveStatus status;

    /**
     * 카테고리 생성용 정적 팩토리 메서드.
     * 외부에서 생성자 대신 이 메서드로 만들면, "새로 만들 땐 항상 ACTIVE 상태"라는 규칙을 한곳에서 보장한다.
     */
    public static Category create(String name) {
        return Category.builder()
                .name(name)
                .status(ActiveStatus.ACTIVE)         // 생성 직후 기본 상태는 활성(ACTIVE)
                .build();
    }
}
