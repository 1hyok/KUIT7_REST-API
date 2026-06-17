package com.kuit.baemin.controller;

import com.kuit.baemin.auth.Login;
import com.kuit.baemin.auth.LoginMember;
import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.RestaurantCreateRequest;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.dto.response.RestaurantDetailResponse;
import com.kuit.baemin.dto.response.RestaurantResponse;
import com.kuit.baemin.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Restaurant", description = "가게 API")   // (Swagger 문서) 이 컨트롤러의 API들을 'Restaurant' 그룹으로 묶음
@RestController
@RequestMapping("/restaurants")                       // 이 컨트롤러의 모든 API 주소는 /restaurants 로 시작
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "가게 등록 (OWNER/ADMIN, 등록자가 점주가 됨)")   // (Swagger) 이 API의 한 줄 설명
    @PostMapping                        // HTTP POST /restaurants 요청을 이 메서드가 처리 (보통 '생성'에 사용)
    public ResponseEntity<ApiResponse<Long>> create(@Login LoginMember member,   // 검증된 토큰에서 꺼낸 현재 로그인 회원(등록자=점주)
                                                    @Valid @RequestBody RestaurantCreateRequest req) {
        Long id = restaurantService.create(member.id(), req);   // 등록자 id를 주인으로
        // 201 Created + Location 헤더(/restaurants/{id}). 이 경로엔 GET 상세조회(getDetail)가 있어 바로 따라갈 수 있음
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(ApiResponse.of(id));
    }

    @Operation(summary = "가게 목록 조회 (페이징, categoryId로 카테고리 필터링)")
    @GetMapping                         // HTTP GET /restaurants (보통 '조회'에 사용)
    public ApiResponse<PageResponse<RestaurantResponse>> list(
            @RequestParam(required = false) Long categoryId,   // 쿼리 파라미터 중 하나. categoryId는 @RequestParam으로 직접 받음 (없어도 됨)
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
            // page·size·sort도 '같은 쿼리스트링'에 함께 들어옴 → 이건 @RequestParam 없이 Pageable이 자동으로 집어감
            // 즉 실제 요청은 둘이 &로 같이 옴: /restaurants?categoryId=1&page=0&size=10&sort=id,desc  (없으면 위 기본값)
        return ApiResponse.of(restaurantService.list(categoryId, pageable));
    }

    @Operation(summary = "가게 상세 조회 (메뉴 목록 포함)")
    @GetMapping("/{restaurantId}")      // GET /restaurants/3 처럼 주소 경로에 id가 들어옴
    public ApiResponse<RestaurantDetailResponse> getDetail(@PathVariable Long restaurantId) {
        // @PathVariable : URL 경로의 {restaurantId} 자리 값을 꺼내서 파라미터로 받음
        return ApiResponse.of(restaurantService.getDetail(restaurantId));
    }
}
