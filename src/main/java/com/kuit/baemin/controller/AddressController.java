package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.AddressCreateRequest;
import com.kuit.baemin.dto.response.AddressResponse;
import com.kuit.baemin.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 회원 배송지(Address) API를 담당하는 컨트롤러 계층.
 * 컨트롤러는 "HTTP 요청을 받아 → Service에 일을 시키고 → 결과를 HTTP 응답으로 돌려주는" 입구 역할만 한다.
 * (실제 DB 처리/검증 같은 비즈니스 로직은 AddressService에 위임한다.)
 *
 * 이 컨트롤러의 URL은 회원에 종속된 자원(중첩 리소스)이라 항상 특정 회원 밑에 있다.
 * 예) POST /members/1/addresses → 1번 회원의 배송지 등록, GET /members/1/addresses → 1번 회원의 배송지 목록
 */
@Tag(name = "Address", description = "회원 배송지 API")          // Swagger 문서에서 이 API들을 묶는 그룹 이름/설명
@RestController
@RequestMapping("/members/{memberId}/addresses")               // 이 클래스의 모든 메서드 공통 URL prefix. {memberId}는 경로 변수(자리표시자)
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;               // 실제 작업을 위임할 서비스. 생성자 주입으로 채워짐

    @Operation(summary = "배송지 등록")                          // Swagger 문서에 표시될 이 API 한 줄 설명
    @PostMapping                                               // HTTP POST + 클래스의 prefix URL 매핑 (POST /members/{memberId}/addresses)
    public ApiResponse<Long> create(@PathVariable Long memberId,
                                    @Valid @RequestBody AddressCreateRequest req) {  // 요청 JSON 본문 → DTO로 변환(@RequestBody), @Valid로 DTO의 검증 규칙 검사
        // 서비스가 생성된 배송지의 PK(Long)를 돌려주고, ApiResponse.of(...)로 공통 응답 형식에 감싸서 반환
        return ApiResponse.of(addressService.create(memberId, req));
    }

    @Operation(summary = "회원 배송지 목록 조회")
    @GetMapping                                               // HTTP GET 매핑 (GET /members/{memberId}/addresses)
    public ApiResponse<List<AddressResponse>> list(@PathVariable Long memberId) {
        // 응답용 DTO(AddressResponse) 리스트를 공통 응답 형식(ApiResponse)에 담아 반환
        return ApiResponse.of(addressService.list(memberId));
    }
}
