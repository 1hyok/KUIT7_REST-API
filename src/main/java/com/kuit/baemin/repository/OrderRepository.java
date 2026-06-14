package com.kuit.baemin.repository;

import com.kuit.baemin.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문(Order) 엔티티에 대한 DB 접근(조회·저장·삭제)을 담당하는 리포지토리 계층.
 *
 * JpaRepository 를 상속(extends)하면 Spring Data JPA 가 실행 시점에 구현체를 자동으로 만들어 준다.
 * (개발자가 SQL/구현 코드를 직접 작성하지 않아도 됨)
 * 제네릭 <Order, Long> 의 의미:
 *   - Order : 이 리포지토리가 다루는 엔티티(= orders 테이블)
 *   - Long  : 그 엔티티의 기본키(PK) 타입
 * 상속만으로 save(저장), findById(PK로 조회), findAll, delete, count, 페이징 등 기본 CRUD 가 제공된다.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 회원별 주문 페이징 조회
    // [파생 쿼리 메서드] 메서드 이름(findBy + MemberId)을 Spring Data JPA 가 해석해
    // Order 의 member.id 로 거르는 SELECT 쿼리를 자동 생성한다. (별도 @Query 불필요)
    // 실제 SQL의 조건 컬럼은 member 를 매핑한 FK 컬럼명 → "WHERE user_id = ?"
    //   (Order 에서 @JoinColumn(name = "user_id") 로 매핑돼 있어 컬럼명이 user_id 다)
    //   - 파라미터 memberId : WHERE 조건에 들어갈 값 (Order 의 member.id 와 매칭)
    //   - Pageable          : 몇 페이지를, 한 페이지에 몇 건, 어떤 정렬로 가져올지 정보 (LIMIT/OFFSET 으로 변환)
    //   - 반환 Page<Order>   : 조회된 주문 목록 + 전체 건수/총 페이지 수 등 페이징 메타데이터까지 함께 담는 타입
    // @EntityGraph: 목록 변환(OrderResponse.from) 때 매번 LAZY 로 끌려오는 가게(restaurant)·주소(address)를
    //               이 조회에서 fetch join 으로 한 번에 함께 가져와 N+1 을 줄인다.
    //               (orderItems 같은 컬렉션은 페이징과 함께 fetch join 하면 메모리 페이징 문제가 생겨 제외 — batch_fetch_size 로 처리)
    @EntityGraph(attributePaths = {"restaurant", "address"})
    Page<Order> findByMemberId(Long memberId, Pageable pageable);
}
