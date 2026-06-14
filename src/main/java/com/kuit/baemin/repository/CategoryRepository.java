package com.kuit.baemin.repository;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리(Category) 영속성 계층(Repository).
 * DB의 category 테이블에 대한 조회/저장/삭제 등을 담당한다.
 *
 * JpaRepository<Category, Long> 을 상속하면
 *  - 첫 번째 타입(Category): 다룰 엔티티
 *  - 두 번째 타입(Long): 그 엔티티의 PK(@Id) 타입
 * 만으로 save / findById / findAll / delete / count 같은 기본 CRUD 메서드를
 * Spring Data JPA가 자동 구현해 준다(직접 코드를 짤 필요 없음).
 *
 * 아래처럼 정해진 규칙으로 메서드 "이름"만 선언하면, Spring Data JPA가
 * 이름을 해석해 SQL을 자동 생성한다(= 파생 쿼리, Derived Query).
 */
// ↓ 인터페이스가 인터페이스를 상속한 것이 맞다. 인터페이스끼리는 implements가 아니라 extends를 쓰고, (클래스와 달리) 여러 개도 상속 가능.
//   JpaRepository도 인터페이스라, 거기 선언된 save/findById/findAll/delete 등 '메서드 선언(계약)'을 그대로 물려받는다(구현체는 Spring Data가 런타임에 생성).
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // existsBy + 필드명(Name) : name 값이 이미 존재하는지 boolean으로 반환
    // -> SELECT ... WHERE name = ? 가 만들어지고, 행이 있으면 true (중복 이름 검사용)
    boolean existsByName(String name);

    // 활성 카테고리 페이징 조회
    // findBy + 필드명(Status) : status가 일치하는 행만 조회
    //   -> SELECT ... WHERE status = ?  (ACTIVE 등 활성 상태만 걸러냄)
    // 파라미터 타입 ActiveStatus : 이 프로젝트가 만든 enum(ACTIVE/INACTIVE) — 소프트삭제 상태값. 여기 ACTIVE를 넘기면 '살아있는' 행만 걸러짐
    // 파라미터 Pageable : (Spring Data가 제공하는 인터페이스) 몇 페이지를 / 한 페이지에 몇 개씩 / 어떤 정렬로 가져올지 정보. 보통 컨트롤러가 ?page=&size=&sort= 요청값으로 채워 넘김
    // 반환 Page<Category> : 해당 페이지의 데이터 + 전체 개수/전체 페이지 수 등 페이징 메타정보까지 함께 담김
    Page<Category> findByStatus(ActiveStatus status, Pageable pageable);
}
