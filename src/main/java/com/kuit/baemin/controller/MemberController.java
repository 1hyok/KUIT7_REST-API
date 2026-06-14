package com.kuit.baemin.controller;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.LoginRequest;
import com.kuit.baemin.dto.request.SignUpRequest;
import com.kuit.baemin.dto.response.MemberResponse;
import com.kuit.baemin.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: 인증/인가 처리 미구현 (8주차에서 다룰 예정)
 *
 * 현재 이 컨트롤러는 JWT 토큰 검증 및 인증/인가 로직이 생략되어 있습니다.
 * 실제 서비스라면 아래 항목들이 반드시 구현되어야 합니다:
 *
 * 1. JWT 토큰 검증
 *    - 요청 헤더(Authorization: Bearer {token})에서 토큰 추출
 *    - 토큰 유효성 검증 (만료 여부, 서명 검증 등)
 *
 * 2. 인증 (Authentication)
 *    - 토큰에서 사용자 정보(memberId 등) 추출
 *    - 현재는 PathVariable이나 RequestBody로 memberId를 직접 받고 있음
 *
 * 3. 인가 (Authorization)
 *    - 요청한 사용자가 해당 리소스에 접근 권한이 있는지 확인
 *    - ex) 본인 주문만 조회 가능, 본인 리뷰만 수정 가능
 *
 *  8주차 커리큘럼(인증, 인가, JWT)을 학습한 후 직접 추가해보세요!
 */
@RestController
@RequestMapping("/members")      // 이 컨트롤러 모든 API의 공통 URL 접두사
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;   // 실제 회원 로직 담당 서비스

    /**
     * POST /members — 회원 가입
     */
    @PostMapping
    public ApiResponse<Long> signUp(@Valid @RequestBody SignUpRequest req) {
        return ApiResponse.of(memberService.signUp(req));
    }

    /**
     * POST /members/login — 로그인
     */
    @PostMapping("/login")
    public ApiResponse<Long> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.of(memberService.login(req));
    }

    /**
     * GET /members/{memberId} — 회원 단건 조회
     */
    @GetMapping("/{memberId}")   // 경로의 {memberId} = 경로 변수(placeholder). URL마다 값이 바뀌는 '빈칸'. /members/7 이면 7, /members/42 면 42가 그 자리에 들어옴
    public ApiResponse<MemberResponse> getMember(@PathVariable Long memberId) {   // @PathVariable: 그 {memberId} 빈칸의 실제 값을 꺼내 파라미터 memberId(Long)로 받음 (이름이 같아서 자동 매칭)
        return ApiResponse.of(memberService.getMember(memberId));
    }
}
