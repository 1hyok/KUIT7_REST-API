package com.kuit.baemin.repository;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.restaurant.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // findBy + 필드명(Status) → WHERE status = ? 인 SELECT 가 생성됨.
    Page<Restaurant> findByStatus(ActiveStatus status, Pageable pageable);

    // findBy + Category_Id + And + Status → WHERE category_id = ? AND status = ?
    // 'CategoryId'는 Restaurant에 categoryId 필드가 있는 게 아니라, 연관 엔티티(category)의 id를
    //   타고 들어가 거르는 것(category.id). JPA가 메서드 이름을 보고 FK 컬럼 category_id로 변환해 줌.
    // → 카테고리별 가게 목록.
    Page<Restaurant> findByCategoryIdAndStatus(Long categoryId, ActiveStatus status, Pageable pageable);
}
