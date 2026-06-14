package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.CategoryCreateRequest;
import com.kuit.baemin.dto.response.CategoryResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 카테고리 Controller — 외부 HTTP 요청이 들어오는 'API 입구'.
 * 요청을 받아 → Service에 일을 시키고 → 그 결과를 응답(JSON)으로 돌려줍니다.
 * (로직은 직접 안 하고 Service에 위임하는 게 원칙 — 입구는 얇게 유지)
 */
@Tag(name = "Category", description = "음식 카테고리 API")   // (Swagger/OpenAPI) Swagger UI 화면에서 이 컨트롤러의 API들을 'Category' 라는 접이식 섹션 하나로 모아 보여줌(name=섹션 제목, description=그 밑 설명). 문서 표시용일 뿐 동작엔 영향 없음
@RestController                                            // 'REST 컨트롤러' 등록 → 메서드 반환값이 자동으로 JSON 응답 본문이 됨
@RequestMapping("/categories")                            // 클래스 레벨 기본 경로. 메서드의 @PostMapping/@GetMapping("/{id}") 경로가 이 뒤에 붙음 → /categories, /categories/{id} ...
@RequiredArgsConstructor                                  // (Lombok) final 필드만 받는 생성자를 자동 생성. 생성자가 이거 하나뿐이라 Spring 이 @Autowired 없이도 이 생성자로 CategoryService 빈을 주입(생성자 주입)
public class CategoryController {

    private final CategoryService categoryService;   // 실제 처리는 이 서비스에 맡김 (생성자로 자동 주입)

    @Operation(summary = "카테고리 생성")   // (Swagger/OpenAPI) 이 메서드(엔드포인트) 하나에 대한 설명. summary = Swagger UI 에서 POST /categories 옆에 뜨는 한 줄 제목. 문서 표시용일 뿐 동작엔 영향 없음
    @PostMapping                          // 'HTTP POST + /categories' 요청을 이 메서드에 연결. @RequestMapping(method=POST)의 단축형. POST=새 데이터 '생성' 용도 (경로 생략 → 클래스의 /categories 그대로)
    public ApiResponse<Long> create(@Valid @RequestBody CategoryCreateRequest req) {
        // @RequestBody : 요청 본문(JSON)을 CategoryCreateRequest 객체로 자동 변환
        // @Valid       : 그 객체의 검증 규칙(@NotBlank 등)을 자동 검사 → 위반 시 400 에러
        return ApiResponse.of(categoryService.create(req));   // ApiResponse = 우리 프로젝트 공통 응답 포맷(성공/코드/결과). 결과로 생성된 카테고리 id 반환
    }

    @Operation(summary = "카테고리 목록 조회 (페이징)")
    @GetMapping                           // 'HTTP GET + /categories' 요청을 이 메서드에 연결. @RequestMapping(method=GET)의 단축형. GET=데이터 '조회'(읽기) 용도
    public ApiResponse<PageResponse<CategoryResponse>> list(   // 반환 타입(안→밖): CategoryResponse(카테고리 1건) → PageResponse(그 카테고리들의 한 페이지+페이징정보) → ApiResponse(공통 응답 봉투)
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
            // @PageableDefault : page·size·sort 같은 페이징 정보를 자동으로 받아 줌. 기본값은 한 페이지 20개, name(이름) 오름차순(가나다순)
        return ApiResponse.of(categoryService.list(pageable));   // PageResponse = 페이지 결과(목록 + 전체 개수·페이지 정보)를 담는 공통 포맷
    }
}
