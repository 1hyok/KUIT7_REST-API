package com.kuit.baemin.controller;

import com.kuit.baemin.auth.Login;
import com.kuit.baemin.auth.LoginMember;
import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.OrderCreateRequest;
import com.kuit.baemin.dto.request.OrderStatusUpdateRequest;
import com.kuit.baemin.dto.response.OrderResponse;
import com.kuit.baemin.dto.response.OrderStatusResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.exception.AuthException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import com.kuit.baemin.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * 주문(Order) 관련 HTTP 요청을 받는 컨트롤러(웹 계층).
 * 클라이언트의 요청을 받아 → 검증 → OrderService에 위임 → 결과를 JSON으로 응답하는 "입구" 역할.
 * 비즈니스 로직(주문 생성/상태 전이/취소 규칙 등)은 직접 처리하지 않고 모두 OrderService가 담당한다.
 */
@Tag(name = "Order", description = "주문 API")  // (Swagger) name="Order"=섹션 제목(화면의 굵은 글씨), description="주문 API"=그 옆 작은 설명. 둘 다 문서 표시용, 동작과 무관
@RestController                                 // = @Controller + @ResponseBody. @ResponseBody 덕분에 각 메서드 반환값을 '뷰 이름'이 아니라 응답 본문으로 직렬화(객체→JSON, HttpMessageConverter가 처리)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;    // 실제 주문 처리를 위임할 서비스. 생성자 주입으로 채워짐 — Spring이 생성자 파라미터 '타입'(OrderService)을 보고 컨테이너에서 그 타입의 빈을 찾아 넣음

    @Operation(summary = "주문 생성 (항목/옵션, 배송지 지정 — 최소 주문 금액 검증)")  // (Swagger) summary=Order 섹션 안에서 이 API 줄(POST /orders) 옆에 뜨는 제목. @Tag는 그룹 라벨, summary는 개별 API 라벨

    @PostMapping("/orders")                                                        // HTTP POST /orders 요청을 이 메서드로 매핑
    // @Login LoginMember: 검증된 토큰에서 꺼낸 현재 로그인 회원(주문자). 회원 id는 member.id()
    // @RequestBody: 요청(들어오는 HTTP) 본문(JSON) → 자바 객체로 '역직렬화'(입력 받기). / @Valid: 그 객체의 검증 제약 검사
    //   ↔ 헷갈림 주의: '뷰 대신 값(JSON)을 응답으로 내보냄'은 반대 방향인 @ResponseBody(여기선 @RestController에 포함)다. @RequestBody=입력, @ResponseBody=출력.
    public ResponseEntity<ApiResponse<Long>> create(@Login LoginMember member,
                                                    @Valid @RequestBody OrderCreateRequest req) {
        // 주문자는 토큰에서만 — 본문으로 받지 않아 사칭 불가
        Long id = orderService.create(member.id(), req);
        // 201 Created + Location 헤더(/orders/{id})
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(ApiResponse.of(id));
    }

    @Operation(summary = "회원별 주문 목록 조회 (페이징, 본인만)")
    @GetMapping("/members/{memberId}/orders")     // HTTP GET. URL 경로의 {memberId} 자리는 아래 @PathVariable로 받음
    public ApiResponse<PageResponse<OrderResponse>> listByMember(
            @PathVariable Long memberId,
            @Login LoginMember member,   // 검증된 토큰에서 꺼낸 현재 로그인 회원
            // Pageable: 페이징 정보(몇 번째 페이지/몇 개씩/정렬)를 담는 객체. ?page=0&size=10&sort=id,desc 같은 쿼리스트링으로 채워짐
            // @PageableDefault: 쿼리스트링이 없을 때 적용할 기본값(한 페이지 10개, id 기준 내림차순 정렬)
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        // 인가: URL의 {memberId}가 토큰 속 '나'와 다르면 남의 주문을 보려는 것 → 403
        if (!memberId.equals(member.id())) {
            throw new AuthException(ErrorStatus.FORBIDDEN);
        }
        return ApiResponse.of(orderService.listByMember(memberId, pageable));
    }

    @Operation(summary = "주문 진행 상태 변경 (가게 주인만, PENDING→ACCEPTED→COOKING→...)")
    @PatchMapping("/orders/{orderId}/status")     // HTTP PATCH = 리소스의 일부(여기선 상태값)만 부분 수정
    public ApiResponse<OrderStatusResponse> changeStatus(@PathVariable Long orderId,
                                                         @Login LoginMember member,   // 검증된 토큰에서 꺼낸 현재 로그인 회원 — 그 주문 가게의 주인인지 확인용
                                                         @Valid @RequestBody OrderStatusUpdateRequest req) {
        // 상태 전이 적법성 + 가게 주인 검증은 서비스가 담당. 여기선 요청 상태값과 호출자 정보를 넘김
        return ApiResponse.of(orderService.changeStatus(orderId, req.getStatus(), member.id(), member.isAdmin()));
    }

    /**
     * DELETE /orders/{orderId} — 주문 취소.
     * 요청자(memberId)는 더 이상 클라이언트가 보내는 값이 아니라, 검증된 토큰에서 꺼낸다.
     * (이전엔 ?memberId=... 로 받아 누구든 사칭 가능했음 → 토큰 기반으로 대체)
     */
    @Operation(summary = "주문 취소 (본인 주문 + 취소 가능 상태만)")
    @ResponseStatus(HttpStatus.NO_CONTENT)         // 삭제/취소처럼 돌려줄 데이터가 없는 작업은 204 No Content (응답 본문 없음)
    @DeleteMapping("/orders/{orderId}")
    public void cancel(@PathVariable Long orderId,    // 취소할 주문 PK는 URL 경로에서
                       @Login LoginMember member) {   // 요청자는 토큰에서 꺼낸 현재 로그인 회원
        orderService.cancel(orderId, member.id());   // 주문이 이 회원 것인지(본인 주문)는 OrderService가 검증(ORDER_FORBIDDEN)
    }                                                // 반환 타입 void + 204 → 성공 시 본문 없이 상태코드만. (다른 엔드포인트의 ApiResponse 봉투와 달리, 데이터 없는 삭제는 REST 관례상 204)
}
