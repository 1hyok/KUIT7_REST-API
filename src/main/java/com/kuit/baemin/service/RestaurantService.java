package com.kuit.baemin.service;

// 'static import' — 클래스 이름 없이 CATEGORY_NOT_FOUND 처럼 상수를 바로 쓰기 위함

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.category.Category;
import com.kuit.baemin.domain.member.Member;
import com.kuit.baemin.domain.menu.Menu;
import com.kuit.baemin.domain.restaurant.Restaurant;
import com.kuit.baemin.dto.request.RestaurantCreateRequest;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.dto.response.RestaurantDetailResponse;
import com.kuit.baemin.dto.response.RestaurantResponse;
import com.kuit.baemin.exception.CategoryException;
import com.kuit.baemin.exception.MemberException;
import com.kuit.baemin.exception.RestaurantException;
import com.kuit.baemin.repository.CategoryRepository;
import com.kuit.baemin.repository.MemberRepository;
import com.kuit.baemin.repository.MenuRepository;
import com.kuit.baemin.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.*;

/**
 * 가게 Service — '비즈니스 로직'을 담당하는 계층.
 * Controller(입구)와 Repository(DB) 사이에서 실제 처리 규칙(검증·조합·변환)을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // 이 클래스의 모든 메서드를 '읽기 전용 트랜잭션'으로 실행 (조회 성능↑). 쓰기는 메서드에 따로 @Transactional을 덧붙임
public class RestaurantService {

    // final + @RequiredArgsConstructor 조합 → 스프링이 생성 시 알맞은 객체를 자동으로 넣어 줌 (new 안 해도 됨)
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;

    /**
     * 가게 등록 — 등록한 회원(ownerId, OWNER/ADMIN)이 이 가게의 주인이 된다.
     */
    @Transactional   // 쓰기(INSERT)가 일어나므로 읽기전용을 덮어쓰는 일반 트랜잭션. 도중 예외 시 자동 롤백됨
    public Long create(Long ownerId, RestaurantCreateRequest req) {
        // 카테고리가 실제 존재하는지 확인. 없으면(Optional이 비면) 예외를 던져 404 응답으로 이어짐
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new CategoryException(CATEGORY_NOT_FOUND));

        // 가게 주인이 될 회원(토큰에서 받은 등록자). 이후 메뉴 등록/주문 상태변경 권한 판단의 기준이 됨
        Member owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

        // 요청 DTO의 값들을 엔티티로 옮겨 담음 (builder 패턴)
        Restaurant restaurant = Restaurant.builder()
                .category(category)
                .owner(owner)                  // 등록자를 점주로 지정
                .name(req.getName())
                .phone(req.getPhone())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .minOrderPrice(req.getMinOrderPrice())
                .deliveryFee(req.getDeliveryFee())
                .status(ActiveStatus.ACTIVE)   // 새 가게는 기본 '영업중'
                .build();

        return restaurantRepository.save(restaurant).getId();   // DB 저장 후, 자동 생성된 id를 반환
    }

    /**
     * 가게 목록 (페이징) — categoryId가 있으면 카테고리로 필터링
     */
    public PageResponse<RestaurantResponse> list(Long categoryId, Pageable pageable) {
        // 삼항 연산자: 조건 ? 참일때 : 거짓일때
        Page<Restaurant> page = (categoryId == null)
                ? restaurantRepository.findByStatus(ActiveStatus.ACTIVE, pageable)
                : restaurantRepository.findByCategoryIdAndStatus(categoryId, ActiveStatus.ACTIVE, pageable);
        // page.map(...) : 조회된 엔티티들을 하나씩 응답 DTO로 변환. RestaurantResponse::from 은 'from 메서드를 넘긴다'는 뜻(메서드 참조)
        return PageResponse.from(page.map(RestaurantResponse::from));
    }

    /**
     * 가게 상세 (메뉴 목록 포함)
     */
    public RestaurantDetailResponse getDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RESTAURANT_NOT_FOUND));
        // 이 가게의 메뉴 중 '숨김(INACTIVE)'이 아닌 것만 조회
        List<Menu> menus = menuRepository.findByRestaurantIdAndStatusNot(restaurantId, ActiveStatus.INACTIVE);
        return RestaurantDetailResponse.of(restaurant, menus);   // 가게 + 메뉴들을 합쳐 상세 응답으로
    }
}
