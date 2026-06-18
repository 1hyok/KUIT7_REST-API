package com.kuit.baemin.domain.address;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.BaseEntity;
import com.kuit.baemin.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;   // DB 레벨 CHECK 제약 생성용

import java.math.BigDecimal;

/**
 * 회원 배송지 엔티티. address 테이블의 한 행 = 이 객체 하나.
 * ERD 관계: 회원(member) 1 : N 배송지(address) — 한 회원이 집/회사 등 여러 배송지를 가질 수 있다.
 * BaseEntity 를 상속해 생성/수정 시각 같은 공통 컬럼을 물려받는다.
 */
@Entity
@Getter
@Builder                                               // 빌더 패턴으로 객체 생성 (Address.builder().road(...).build())
@Table(name = "address")
// @Check = Hibernate가 테이블에 'CHECK 제약'(DB 무결성 규칙)을 생성. 조건을 어기는 값은 DB가 INSERT/UPDATE 거부.
//   status 컬럼엔 'active'/'inactive'만, type 컬럼엔 'home'/'work'/'etc'만 허용 (컨버터가 소문자로 저장하므로 값이 정확히 일치).
//   주의: 이 앱은 ddl-auto=none → 이 제약이 자동으로 DB에 안 걸림. 엔티티에서 schema.sql 재생성 후 그 DDL을 DB에 실행해야 실제 적용됨.
@Check(constraints = "status in ('active','inactive') and type in ('home','work','etc')")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 이 배송지의 주인 회원 ---
    @ManyToOne(fetch = FetchType.LAZY)                 // N:1 관계(배송지 N : 회원 1). LAZY = member를 실제로 꺼내 쓸 때만 DB 조회
    @JoinColumn(name = "user_id", nullable = false)   // 이 테이블에 만들 외래키(FK) 컬럼명. ERD: address.user_id
    private Member member;

    // ERD: address.type ENUM('home','work','etc') — AddressTypeConverter 가 소문자로 매핑
    @Column(nullable = false, length = 10)
    private AddressType type;

    @Column(length = 50)
    private String alias;                                 // 배송지 별칭 = 사용자가 그 주소에 붙이는 이름 (예: "우리집"·"회사"). nullable 미지정 → 선택값(없어도 됨)

    @Column(name = "road_address", nullable = false, length = 255)
    private String roadAddress;                           // 도로명 주소 (예: "서울 강남구 테헤란로 123"). nullable=false → 필수

    @Column(name = "detail_address", length = 255)
    private String detailAddress;                         // 상세 주소 (예: "101동 1502호", "3층"). nullable 미지정 → 선택값

    @Column(precision = 10, scale = 7)                 // 위도. precision=전체 자리수, scale=소수점 이하 자리수 (예: 37.5665000)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)                 // 경도. 좌표는 오차에 민감해 실수 대신 BigDecimal 로 정확히 저장
    private BigDecimal longitude;

    // ActiveStatus(ACTIVE/INACTIVE 등)도 type 과 마찬가지로 Converter 가 DB 문자열로 매핑
    @Column(nullable = false, length = 10)
    private ActiveStatus status;

    /** 이 배송지가 주어진 회원의 소유인지 검사 (다른 사람 배송지를 건드리지 못하게 권한 확인용) */
    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);  // 배송지 주인의 id 와 요청자 id 가 같은지 비교
    }
}
