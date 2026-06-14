package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.MenuCreateRequest;
import com.kuit.baemin.dto.response.MenuResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 메뉴(Menu) 관련 HTTP 요청을 받는 컨트롤러(웹 계층).
 * - 컨트롤러의 책임: 들어온 요청을 받아 검증/파라미터 추출만 하고, 실제 처리는 Service에 위임한다.
 * - "특정 가게(restaurant)에 속한 메뉴"를 다루므로 URL이 가게 ID 하위 경로로 설계됨.
 *   예: POST /restaurants/3/menus  -> 3번 가게에 메뉴 등록
 */
@Tag(name = "Menu", description = "메뉴 API")            // Swagger 문서에서 이 API들을 "Menu" 그룹으로 묶어 표시
@RestController
@RequestMapping("/restaurants/{restaurantId}/menus")    // 이 컨트롤러의 공통 URL 접두사. {restaurantId}는 경로 변수(placeholder)
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;              // 실제 메뉴 처리 로직을 가진 서비스. 생성자 주입으로 스프링이 넣어줌

    @Operation(summary = "메뉴 등록 (옵션 그룹/옵션 포함)")  // Swagger UI 'Menu' 섹션 안, "POST /restaurants/{restaurantId}/menus" 줄 오른쪽에 뜨는 회색 제목(요약). 그 줄을 펼치면 상단에도 같은 문구가 보임
    @PostMapping                                        // "POST 방식으로 이 경로(/restaurants/{restaurantId}/menus)에 요청이 들어오면 → 바로 아래 create() 메서드를 실행해라"라고 Spring에게 연결(매핑)해 주는 표지판. (POST = 새로 만들기 요청)
    public ApiResponse<Long> create(@PathVariable Long restaurantId,   // @PathVariable: URL '경로'의 {restaurantId} 자리 값을 꺼내 이 파라미터로 받음 (POST /restaurants/3/menus → 3)
                                    @Valid @RequestBody MenuCreateRequest req) {   // @RequestBody: 클라이언트가 보낸 요청 '본문(JSON)'을 MenuCreateRequest 객체로 자동 변환해 req에 담음
                                                                               // @Valid: 그 req의 필드에 붙은 검증 규칙(@NotBlank/@NotNull/@Positive 등)을 메서드 실행 '전에' 검사. 어기면 400 에러로 막고 create() 본문은 실행 안 함
        // 등록 성공 시 생성된 메뉴의 PK(Long)를 공통 응답 포맷(ApiResponse)에 담아 반환
        return ApiResponse.of(menuService.create(restaurantId, req));
    }

    @Operation(summary = "가게의 메뉴 목록 조회 (페이징)")
    @GetMapping                                         // HTTP GET. 클래스 공통 경로로 들어오는 목록 조회 요청을 처리
    public ApiResponse<PageResponse<MenuResponse>> list(
            @PathVariable Long restaurantId,
            // Pageable: 페이징 정보(몇 페이지, 한 페이지 몇 개, 정렬)를 담는 객체. 쿼리 파라미터(?page=0&size=20&sort=...)로 자동 채워짐
            // @PageableDefault: 클라이언트가 값을 안 주면 적용할 기본값
            //   sort = "id" : "정렬 기준 = id 필드". 즉 결과 목록을 메뉴의 id 순서로 줄 세움 (DB에 ORDER BY id 가 붙음)
            //   direction = ASC : 오름차순(작은 id가 먼저 → 1, 2, 3...). DESC면 내림차순(최신 id 먼저)
            //   size = 20 : 한 페이지 20개. 이 값들은 요청에 ?sort=...&size=... 가 없을 때만 쓰이는 '기본값'
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        // 조회된 한 페이지 분량의 메뉴 목록(MenuResponse들 + 페이지 정보)을 PageResponse로 감싸 반환
        return ApiResponse.of(menuService.list(restaurantId, pageable));
    }
}
