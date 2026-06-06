package com.kuit.baemin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티가 공통으로 갖는 "생성/수정 시각" 필드를 모아둔 부모 클래스.
 * 각 엔티티(Member, Restaurant 등)가 이 클래스를 상속하면 createdAt/updatedAt 컬럼을 물려받는다.
 * 이 두 필드(createdAt/updatedAt)에 들어갈 시각 값은 JPA Auditing 이 자동으로 채워준다(우리가 직접 set 하지 않음) → 작동하려면 시작 클래스(BaeminApplication)에 @EnableJpaAuditing 이 있어야 함(실제로 있음).
 */
@Getter
@MappedSuperclass                                  // 이 클래스 자체는 테이블이 아님. 필드만 자식 엔티티 테이블에 합쳐짐(공통 컬럼 상속용)
@EntityListeners(AuditingEntityListener.class)     // 저장/수정 시점을 감지해 아래 시각 필드를 자동으로 채워주는 리스너 등록
public abstract class BaseEntity {

    @CreatedDate                                   // 처음 INSERT 될 때 현재 시각이 한 번 자동 저장됨
    @Column(nullable = false, updatable = false)   // nullable=false: NULL 불가(반드시 값 있어야 함=필수) · updatable=false: 처음 저장 후 못 바꿈(생성 시각 불변)
    private LocalDateTime createdAt;                // 생성 시각 = 이 행(데이터)이 DB에 처음 만들어진 때

    @LastModifiedDate                              // INSERT 및 이후 매 UPDATE 마다 현재 시각으로 자동 갱신됨
    @Column(nullable = false)
    private LocalDateTime updatedAt;                // 수정 시각 = 이 행이 가장 최근에 바뀐 때
}
