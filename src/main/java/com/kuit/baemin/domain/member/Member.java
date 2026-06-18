package com.kuit.baemin.domain.member;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;   // DB 레벨 CHECK 제약(허용값만 통과) 생성용 — 잘못된 값이 컨버터를 우회해 들어오는 것 방지

/**
 * 회원 엔티티 (ERD: user 테이블).
 *
 * <p>엔티티(@Entity) = DB 테이블과 1:1로 짝지어진 클래스. 이 객체 하나가 user 테이블의 한 행(row)에 해당한다.
 * 즉 회원 한 명 = Member 객체 하나 = user 테이블 한 줄.
 *
 * <p>BaseEntity 를 상속하므로 생성/수정 시각(createdAt, updatedAt) 컬럼을 자동으로 함께 가진다.
 */
@Entity                                              // 이 클래스를 JPA가 관리하는 DB 테이블로 매핑
@Getter
@Builder                                             // 빌더 패턴 생성(Member.builder().email(..).build()) — 어떤 값을 넣는지 코드에 드러남
@Table(name = "user")                                // ERD 테이블명에 맞춤 (단수)
@Check(constraints = "status in ('active','inactive')")   // status 컬럼엔 이 두 값만 허용 (role 은 이미 ENUM 타입이라 별도 불필요)
@AllArgsConstructor                                  // @Builder 가 내부적으로 쓰는 전체 필드 생성자
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA가 객체를 만들 때 쓰는 기본 생성자(필수). PROTECTED = 외부의 무분별한 new 차단
public class Member extends BaseEntity {

    @Id                                              // 이 필드가 테이블의 기본키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // PK를 DB가 auto_increment로 자동 부여 (INSERT 시 채워짐)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)   // ERD: email VARCHAR(255), UNIQUE
    private String email;

    @Column(nullable = false, length = 255)                  // ERD: password VARCHAR(255)
    private String password;

    @Column(nullable = false, length = 100)                  // ERD: name VARCHAR(100)
    private String name;

    @Column(nullable = false, length = 20, unique = true)    // ERD: phone VARCHAR(20), UNIQUE
    private String phone;

    // ERD: status ENUM('active','inactive'). 자바 enum은 대문자(ACTIVE)지만 DB는 소문자라서
    // ActiveStatusConverter(@Converter(autoApply=true))가 ACTIVE <-> "active" 로 자동 변환한다.
    // 변환기가 매핑을 책임지므로 이 필드엔 @Enumerated 를 붙이면 안 됨(충돌).
    @Column(nullable = false, length = 10)
    private ActiveStatus status;

    // 회원 권한(인가용). status 와 달리 별도 컨버터 없이 enum 이름을 그대로 저장한다(예: CONSUMER).
    // @Enumerated 는 자바 enum 을 DB 컬럼에 저장하는 '방식'을 정하는 JPA 어노테이션이다.
    //   STRING  → enum 이름을 문자열로 저장. 순서가 바뀌어도 의미가 안 깨져 보통 이걸 권장.
    //   ORDINAL → 선언 순서 번호를 저장. 순서를 바꾸거나 중간에 끼우면 기존 데이터 의미가 어긋나 위험.
    // 실행 전 user 테이블에 role 컬럼을 추가해야 한다(마이그레이션 SQL 은 README 참고).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;
}
