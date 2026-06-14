package com.kuit.baemin.repository;

import com.kuit.baemin.domain.menu.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MenuOption(메뉴 옵션) 엔티티의 DB 접근(영속성) 계층.
 *
 * Repository 는 "DB와 대화하는 창구" 역할을 한다. Service 가 이 인터페이스를 호출하면
 * 실제 SQL 실행은 Spring Data JPA 가 대신 처리해 준다.
 *
 * JpaRepository<MenuOption, Long> 를 상속받으면:
 *  - 첫 번째 타입(MenuOption) = 다룰 엔티티(= menu_option 테이블 한 행)
 *  - 두 번째 타입(Long)       = 그 엔티티의 PK(기본키) 타입
 *  - save / findById / findAll / delete / findAllById 등 기본 CRUD 메서드를 따로 구현하지 않아도 자동 제공받는다.
 */
public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {
    // 별도 메서드 선언 없이 JpaRepository 기본 CRUD만 사용한다.
    // (주문 생성 시 OrderService.buildOrderItem 이 findById(optionId)로 옵션을 하나씩 조회·검증함)
}
