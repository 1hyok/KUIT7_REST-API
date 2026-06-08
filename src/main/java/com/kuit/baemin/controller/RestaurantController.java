package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.RestaurantCreateReq;
import com.kuit.baemin.dto.response.PageRes;
import com.kuit.baemin.dto.response.RestaurantDetailRes;
import com.kuit.baemin.dto.response.RestaurantRes;
import com.kuit.baemin.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Restaurant", description = "가게 API")   // (Swagger 문서) 이 컨트롤러의 API들을 'Restaurant' 그룹으로 묶음
@RestController
@RequestMapping("/restaurants")                       // 이 컨트롤러의 모든 API 주소는 /restaurants 로 시작
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "가게 등록")   // (Swagger) 이 API의 한 줄 설명
    @PostMapping                        // HTTP POST /restaurants 요청을 이 메서드가 처리 (보통 '생성'에 사용)
    public ApiResponse<Long> create(@Valid @RequestBody RestaurantCreateReq req) {
        return ApiResponse.of(restaurantService.create(req));   // ApiResponse = 우리 프로젝트 공통 응답 포맷(성공/코드/결과)
    }

    @Operation(summary = "가게 목록 조회 (페이징, categoryId로 카테고리 필터링)")
    @GetMapping                         // HTTP GET /restaurants (보통 '조회'에 사용)
    public ApiResponse<PageRes<RestaurantRes>> list(
            @RequestParam(required = false) Long categoryId,   // 쿼리 파라미터: /restaurants?categoryId=1 의 값. required=false라 없어도 됨
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
            // @PageableDefault : page·size·sort 같은 페이징 정보를 자동으로 받아 줌. 기본값은 한 페이지 10개, id 내림차순(최신순)
        return ApiResponse.of(restaurantService.list(categoryId, pageable));
    }

    @Operation(summary = "가게 상세 조회 (메뉴 목록 포함)")
    @GetMapping("/{restaurantId}")      // GET /restaurants/3 처럼 주소 경로에 id가 들어옴
    public ApiResponse<RestaurantDetailRes> getDetail(@PathVariable Long restaurantId) {
        // @PathVariable : URL 경로의 {restaurantId} 자리 값을 꺼내서 파라미터로 받음
        return ApiResponse.of(restaurantService.getDetail(restaurantId));
    }
}
