package com.kuit.baemin.domain.menu;

import com.kuit.baemin.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 메뉴 옵션 (ERD: menu_option) — OPTION_GROUP 1 : N MENU_OPTION
 *
 * <p>menu_option 테이블의 한 행 = 이 객체 하나. (예: "곱빼기 +1,000원", "맵게 +0원")
 * 하나의 옵션 그룹(OptionGroup, 예: "면 양 선택")에 여러 개의 선택지(MenuOption)가 묶인다.
 * 생성/수정 시각 등 공통 컬럼은 BaseEntity 를 상속받아 물려받는다.
 */
@Entity
@Getter
@Builder
// 복합 UNIQUE 제약: (option_group_id, name) '조합'이 유일해야 함 = 한 옵션 그룹 안에서 옵션 이름 중복 금지. 다른 그룹이면 같은 이름 OK.
//   예) '맵기' 그룹에 '보통' 두 개 → 거부 / '맵기'의 '보통' + '양'의 '보통' → 허용(그룹이 다르니 조합이 다름)
//   columnNames = 묶을 컬럼들 / name="uk_..." = 이 제약의 DB 이름(나중에 참조·삭제 쉽게). 업무 규칙 가정이라 안 맞으면 제거.
//   주의: ddl-auto=none → 이 제약도 자동 적용 안 됨. schema.sql 재생성 후 DB에 실행해야 실제로 걸린다.
@Table(name = "menu_option",
       uniqueConstraints = @UniqueConstraint(name = "uk_menu_option_group_name", columnNames = {"option_group_id", "name"}))
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 연관관계: 어떤 옵션 그룹(OptionGroup)에 속한 옵션인지 ---
    @Setter                                              // 이 FK 필드만 외부에서 교체 가능 (양방향 연관관계 세팅 등에 사용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false) // 이 테이블에 만들 외래키(FK) 컬럼명. nullable=false → 옵션은 반드시 그룹에 소속
    private OptionGroup optionGroup;                    // 자바에선 OptionGroup '객체' 자체(그룹 전체 접근 가능). DB엔 그 객체의 id만 위 option_group_id 컬럼에 저장됨

    @Column(nullable = false, length = 100)              // NOT NULL, 최대 길이 100인 VARCHAR 컬럼
    private String name;

    @Column(name = "extra_price", nullable = false)      // 옵션 가격(메뉴 가격에 더해지는 금액, 0이면 무료). DB 컬럼명은 기존 extra_price 그대로 유지(스키마 안 깨지게)
    private int price;
}
