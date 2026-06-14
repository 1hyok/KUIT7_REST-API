package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.domain.order.OrderStatus;
import com.kuit.baemin.dto.request.OrderCreateRequest;
import com.kuit.baemin.dto.request.OrderStatusUpdateRequest;
import com.kuit.baemin.dto.response.OrderResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 주문(Order) 관련 HTTP 요청을 받는 컨트롤러(웹 계층).
 * 클라이언트의 요청을 받아 → 검증 → OrderService에 위임 → 결과를 JSON으로 응답하는 "입구" 역할.
 * 비즈니스 로직(주문 생성/상태 전이/취소 규칙 등)은 직접 처리하지 않고 모두 OrderService가 담당한다.
 */
@Tag(name = "Order", description = "주문 API")  // (Swagger) name="Order"=섹션 제목(화면의 굵은 글씨), description="주문 API"=그 옆 작은 설명. 둘 다 문서 표시용, 동작과 무관
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;    // 실제 주문 처리를 위임할 서비스. 생성자 주입으로 채워짐

    @Operation(summary = "주문 생성 (항목/옵션, 배송지 지정 — 최소 주문 금액 검증)")  // (Swagger) summary=Order 섹션 안에서 이 API 줄(POST /orders) 옆에 뜨는 제목. @Tag는 그룹 라벨, summary는 개별 API 라벨

    @PostMapping("/orders")                                                        // HTTP POST /orders 요청을 이 메서드로 매핑
    // @RequestBody: 요청 본문(JSON)을 OrderCreateRequest 객체로 변환
    // @Valid: 변환된 객체의 Bean Validation 제약(@NotNull 등)을 검사. 위반 시 메서드 실행 전 예외 발생
    public ApiResponse<Long> create(@Valid @RequestBody OrderCreateRequest req) {
        return ApiResponse.of(orderService.create(req));                           // 생성된 주문의 PK(Long)를 공통 응답 포맷에 담아 반환
    }

    @Operation(summary = "회원별 주문 목록 조회 (페이징)")
    @GetMapping("/members/{memberId}/orders")     // HTTP GET. URL 경로의 {memberId} 자리는 아래 @PathVariable로 받음
    public ApiResponse<PageResponse<OrderResponse>> listByMember(
            @PathVariable Long memberId,
            // Pageable: 페이징 정보(몇 번째 페이지/몇 개씩/정렬)를 담는 객체. ?page=0&size=10&sort=id,desc 같은 쿼리스트링으로 채워짐
            // @PageableDefault: 쿼리스트링이 없을 때 적용할 기본값(한 페이지 10개, id 기준 내림차순 정렬)
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.of(orderService.listByMember(memberId, pageable));
    }

    @Operation(summary = "주문 진행 상태 변경 (PENDING→ACCEPTED→COOKING→...)")
    @PatchMapping("/orders/{orderId}/status")     // HTTP PATCH = 리소스의 일부(여기선 상태값)만 부분 수정
    public ApiResponse<OrderStatus> changeStatus(@PathVariable Long orderId,
                                                 @Valid @RequestBody OrderStatusUpdateRequest req) {
        // 실제 상태 전이가 허용되는지(PENDING→ACCEPTED 등) 검증은 서비스/도메인이 담당. 여기선 요청한 상태값만 넘김
        return ApiResponse.of(orderService.changeStatus(orderId, req.getStatus()));
    }

    /**
     * DELETE /orders/{orderId} — 주문 취소
     * 인증 미구현 단계라 RequestParam으로 memberId를 받음 (8주차에서 토큰 기반으로 대체)
     */
    @Operation(summary = "주문 취소 (본인 주문 + 취소 가능 상태만)")
    @DeleteMapping("/orders/{orderId}")
    public ApiResponse<Void> cancel(@PathVariable Long orderId,    // 취소할 주문 PK는 URL 경로에서
                                    @RequestParam Long memberId) {  // 요청자 식별용 memberId는 쿼리스트링(?memberId=...)에서
        orderService.cancel(orderId, memberId);
        return ApiResponse.of(null);              // 반환할 데이터가 없는 작업이라 본문 data는 null(성공 여부만 응답)
    }
}
