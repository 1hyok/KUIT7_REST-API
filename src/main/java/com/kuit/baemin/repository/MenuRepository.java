package com.kuit.baemin.repository;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.menu.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 가게의 메뉴 페이징 조회 — '쿼리 메서드'(메서드 이름 규칙대로 지으면 JPA가 SQL 자동 생성)
    // findBy + Restaurant_Id → WHERE restaurant_id = ? 인 SELECT 가 생성됨 (연관 엔티티 Restaurant의 id로 거름)
    Page<Menu> findByRestaurantId(Long restaurantId, Pageable pageable);

    // 가게 상세용 — 숨김(INACTIVE) 제외 메뉴 목록
    // findBy + Restaurant_Id + And + Status + Not → WHERE restaurant_id = ? AND status <> ?
    // (StatusNot = "status가 인자와 '다른' 행만". 인자로 INACTIVE를 넘기면 → 활성 메뉴만 남음)
    List<Menu> findByRestaurantIdAndStatusNot(Long restaurantId, ActiveStatus status);
}
