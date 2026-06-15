package com.kuit.baemin.service;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.address.Address;
import com.kuit.baemin.domain.member.Member;
import com.kuit.baemin.domain.menu.Menu;
import com.kuit.baemin.domain.menu.MenuOption;
import com.kuit.baemin.domain.order.Order;
import com.kuit.baemin.domain.order.OrderItem;
import com.kuit.baemin.domain.order.OrderItemOption;
import com.kuit.baemin.domain.order.OrderStatus;
import com.kuit.baemin.domain.restaurant.Restaurant;
import com.kuit.baemin.dto.request.OrderCreateRequest;
import com.kuit.baemin.dto.request.OrderItemRequest;
import com.kuit.baemin.dto.response.OrderResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.exception.*;
import com.kuit.baemin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.*;

/**
 * 주문(Order) 도메인의 비즈니스 로직을 담당하는 서비스 계층.
 * Controller(요청 처리)와 Repository(DB 접근) 사이에서 "주문을 어떻게 만들고/취소하고/상태를 바꿀지"의 규칙을 모아둔다.
 * (예: 가게가 영업 중인지, 주소가 본인 것인지, 최소 주문 금액을 넘는지 등 검증 → 통과해야만 저장)
 */
@Service                            // @Component의 특화형 — 기능(스캔→빈 등록)은 동일, 차이는 '의미' 표시: 이 클래스가 비즈니스 로직(서비스 계층)임을 나타냄(가독성·AOP 대상 지정에 유리)
@RequiredArgsConstructor
@Transactional(readOnly = true)     // 클래스의 모든 메서드는 기본적으로 "읽기 전용" 트랜잭션. 쓰기가 필요한 메서드는 아래처럼 @Transactional 로 덮어쓴다
public class OrderService {

    // --- 의존성: 이 서비스가 일을 하려면 필요한 Repository(=DB 접근 통로)들 ---
    // final + @RequiredArgsConstructor 조합이라 생성자를 통해 스프링이 자동으로 채워준다
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    /**
     * 주문 생성 — 항목/옵션 구성, 최소 주문 금액 검증, 금액 계산.
     * 흐름: 회원/가게/주소 존재 및 권한 검증 → 주문 항목 만들기 → 최소 주문 금액 체크 → Order 저장 후 PK 반환.
     */
    @Transactional                          // 데이터를 저장(쓰기)하므로 읽기전용을 덮어써 쓰기 트랜잭션으로 실행. 중간에 예외가 나면 전체 롤백
    public Long create(Long memberId, OrderCreateRequest req) {   // memberId는 컨트롤러가 인증 토큰에서 꺼내 넘김(본문 신뢰 X)
        // findById 는 Optional 을 돌려줌 → 값이 없으면 orElseThrow 로 예외를 던져 흐름 중단 (없는 회원/가게/주소 차단)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(req.getRestaurantId())
                .orElseThrow(() -> new RestaurantException(RESTAURANT_NOT_FOUND));
        if (!restaurant.isActive()) {       // 폐업/일시중지 등 영업 중이 아닌 가게에는 주문 불가
            throw new RestaurantException(RESTAURANT_NOT_ACTIVE);
        }

        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));
        if (!address.isOwnedBy(member.getId())) {   // 남의 배송지로 주문하는 것 방지 (소유자 검증)
            throw new AddressException(ADDRESS_FORBIDDEN);
        }

        // 요청에 담긴 항목 목록을 하나씩 OrderItem 으로 변환 (메뉴/옵션 검증은 buildOrderItem 안에서)
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemReq : req.getItems()) {
            items.add(buildOrderItem(restaurant.getId(), itemReq));
        }

        // 항목별 소계(subtotal)를 모두 더해 음식 합계를 구하고, 가게의 최소 주문 금액 미만이면 주문 거절
        int itemsTotal = items.stream().mapToInt(OrderItem::getSubtotal).sum();
        if (itemsTotal < restaurant.getMinOrderPrice()) {
            throw new OrderException(ORDER_BELOW_MIN_PRICE);
        }

        // 검증을 모두 통과하면 주문 엔티티를 만든다 (초기 상태 PENDING, 총액 = 음식 합계 + 배달비)
        Order order = Order.builder()
                .member(member)
                .restaurant(restaurant)
                .address(address)
                .orderStatus(OrderStatus.PENDING)       // 주문 진행 상태의 시작점: 접수 대기
                .status(ActiveStatus.ACTIVE)            // 소프트 삭제용 활성 상태 (DB에서 지우지 않고 ACTIVE/INACTIVE 로 관리)
                .deliveryFee(restaurant.getDeliveryFee())   // 주문 시점 배달비를 박제 (이후 가게가 배달비를 바꿔도 이 주문엔 영향 없음)
                .totalPrice(itemsTotal + restaurant.getDeliveryFee())
                .build();
        // 연관관계 편의 메서드: order 의 자식 목록에 추가하면서 OrderItem 쪽에도 order 를 세팅(양방향 연결)
        items.forEach(order::addOrderItem);

        // save 후 DB가 부여한 PK(id)를 꺼내 반환 (보통 Controller 는 이 id 로 생성 결과를 응답)
        return orderRepository.save(order).getId();
    }

    /** 주문 요청 항목 1건 → OrderItem 으로 변환. 메뉴가 이 가게 것인지/주문 가능한지, 옵션이 이 메뉴 것인지 검증 */
    private OrderItem buildOrderItem(Long restaurantId, OrderItemRequest itemReq) {
        Menu menu = menuRepository.findById(itemReq.getMenuId())
                .orElseThrow(() -> new MenuException(MENU_NOT_FOUND));
        if (!menu.getRestaurant().getId().equals(restaurantId)) {   // 다른 가게 메뉴를 섞어 주문하는 것 방지
            throw new MenuException(MENU_NOT_IN_RESTAURANT);
        }
        if (!menu.isOrderable()) {                                  // 품절/판매중지 메뉴 차단
            throw new MenuException(MENU_NOT_ORDERABLE);
        }

        // priceAtOrder: 주문 시점의 메뉴 가격을 그대로 기록 (이후 메뉴 가격이 바뀌어도 과거 주문 금액은 보존)
        OrderItem item = OrderItem.builder()
                .menu(menu)
                .quantity(itemReq.getQuantity())
                .priceAtOrder(menu.getPrice())
                .build();

        // 선택한 옵션들을 한 번의 IN 쿼리로 모두 조회 (예전: 옵션마다 findById → 옵션 수만큼 쿼리(N+1). 지금: findAllById 한 번)
        List<Long> optionIds = itemReq.getOptionIds();
        List<MenuOption> options = menuOptionRepository.findAllById(optionIds);
        if (options.size() != optionIds.stream().distinct().count()) {   // 못 찾은(존재하지 않는) 옵션 id 가 섞이면 조회 개수가 안 맞음
            throw new MenuException(MENU_OPTION_NOT_FOUND);
        }
        for (MenuOption option : options) {
            // 이 옵션이 정말 '이 메뉴'의 옵션인지 확인 (옵션 → 옵션그룹 → 메뉴로 거슬러 올라가 메뉴 id 비교)
            // 왜 필요? 클라이언트가 menuId와 optionIds를 '따로' 보내므로, "후라이드 주문 + 짜장면 옵션"처럼 엉뚱한 조합을 막아야 함.
            //   위 개수검사(120줄)는 '옵션이 존재하나'만 보고, 이 검사는 '그 옵션이 이 메뉴 소속이냐'를 봄 — 둘은 막는 게 다름.
            if (!option.getOptionGroup().getMenu().getId().equals(menu.getId())) {
                throw new MenuException(INVALID_MENU_OPTION);
            }
            item.addOption(OrderItemOption.of(option));     // of(...): MenuOption(메뉴 카탈로그) → OrderItemOption(주문 시점 스냅샷)으로 가격을 복사해 변환.
            //   둘을 따로 두는 이유: 나중에 가게가 옵션 가격을 바꿔도, '과거 주문 금액'은 주문 당시 값(priceAtOrder)으로 그대로 보존하려고. MenuOption만 참조하면 과거 주문 금액이 소급해 바뀜
        }
        return item;
    }

    /**
     * 회원별 주문 목록 (페이징).
     * 메서드 자체는 읽기 전용(@Transactional(readOnly=true) 가 클래스에 걸려 있음).
     */
    public PageResponse<OrderResponse> listByMember(Long memberId, Pageable pageable) {
        // existsById: 엔티티 전체를 불러오지 않고 "존재 여부"만 가볍게 확인 (없으면 예외)
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MEMBER_NOT_FOUND);
        }
        // findByMemberId: 메서드 이름으로 SQL 이 자동 생성되는 파생 쿼리 (where member_id = ?), Pageable 로 페이지 단위 조회
        // .map(OrderResponse::from): 조회된 Order 엔티티 Page 를 응답용 DTO(OrderResponse) Page 로 변환 (엔티티를 그대로 노출하지 않기 위함)
        // PageResponse.from(...): Spring 의 Page 를 우리 응답 형식(PageResponse)으로 한 번 더 감싸 반환
        return PageResponse.from(
                orderRepository.findByMemberId(memberId, pageable)
                        .map(OrderResponse::from)
        );
    }

    /**
     * 주문 취소 (본인 주문 + 취소 가능 상태만).
     */
    @Transactional                          // 상태를 바꿔 저장하므로 쓰기 트랜잭션
    public void cancel(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));
        if (!order.isOwnedBy(memberId)) {               // 남의 주문을 취소하지 못하도록 소유자 검증
            throw new OrderException(ORDER_FORBIDDEN);
        }
        if (!order.getOrderStatus().isCancelable()) {   // 이미 배달 중/완료 등 취소 불가 상태면 거절 (enum 이 상태 전이 규칙을 보유)
            throw new OrderException(ORDER_NOT_CANCELABLE);
        }
        // 영속(영속성 컨텍스트에 올라온) 엔티티의 값을 바꾸면, 트랜잭션 종료 시 변경 감지(dirty checking)로 자동 UPDATE 됨 → 별도 save 불필요
        order.cancel();
    }

    /**
     * 주문 진행 상태 변경 (가게/관리자 관점 — 종료 상태는 변경 불가).
     */
    @Transactional                          // 상태를 바꿔 저장하므로 쓰기 트랜잭션
    public OrderStatus changeStatus(Long orderId, OrderStatus next, Long actorMemberId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));
        // 인가: 그 주문이 발생한 가게의 주인(점주)만 상태를 바꿀 수 있다. ADMIN은 전부 허용
        if (!isAdmin && !order.getRestaurant().isOwnedBy(actorMemberId)) {
            throw new OrderException(ORDER_FORBIDDEN);
        }
        // 진행 상태는 '바로 다음 단계'로만 변경 가능 (역방향·건너뛰기 금지, 취소는 cancel() 경로로만 → 여기선 CANCELED도 거부)
        if (!order.getOrderStatus().canProceedTo(next)) {
            throw new OrderException(ORDER_STATUS_NOT_CHANGEABLE);
        }
        order.changeStatus(next);                       // 변경 감지로 트랜잭션 종료 시 자동 반영
        return order.getOrderStatus();
    }
}
