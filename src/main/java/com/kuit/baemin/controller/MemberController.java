package com.kuit.baemin.controller;

import com.kuit.baemin.auth.AuthService;
import com.kuit.baemin.auth.dto.TokenResponse;
import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.LoginRequest;
import com.kuit.baemin.dto.request.SignUpRequest;
import com.kuit.baemin.dto.response.MemberResponse;
import com.kuit.baemin.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 API 컨트롤러.
 *
 * <p>인증/인가(8주차)는 Spring Security로 구현됨:
 * <ul>
 *   <li>JWT 토큰 검증 — Authorization: Bearer 헤더의 토큰을 Spring Security(리소스 서버)가 자동 검증 (SecurityConfig 참고)</li>
 *   <li>인증 — 로그인 성공 시 access/refresh 토큰 발급(login). 보호 API는 토큰이 있어야 접근 가능</li>
 *   <li>인가 — 회원 id는 PathVariable이 아니라 토큰(@AuthenticationPrincipal)에서 꺼내, '본인 것만' 접근하도록 함 (Order/Address 컨트롤러 참고)</li>
 * </ul>
 */
@RestController
@RequestMapping("/members")      // 이 컨트롤러 모든 API의 공통 URL 접두사
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;   // 실제 회원 로직 담당 서비스
    private final AuthService authService;        // 로그인 시 토큰 발급을 담당

    /**
     * POST /members — 회원 가입
     */
    @PostMapping
    public ApiResponse<Long> signUp(@Valid @RequestBody SignUpRequest req) {
        return ApiResponse.of(memberService.signUp(req));
    }

    /**
     * POST /members/login — 로그인. 성공하면 access/refresh 토큰을 발급해 돌려준다.
     * (이전엔 memberId(Long)만 반환했지만, 8주차부터 토큰을 반환)
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.of(authService.login(req));
    }

    /**
     * GET /members/{memberId} — 회원 단건 조회
     */
    @GetMapping("/{memberId}")   // 경로의 {memberId} = 경로 변수(placeholder). URL마다 값이 바뀌는 '빈칸'. /members/7 이면 7, /members/42 면 42가 그 자리에 들어옴
    public ApiResponse<MemberResponse> getMember(@PathVariable Long memberId) {   // @PathVariable: 그 {memberId} 빈칸의 실제 값을 꺼내 파라미터 memberId(Long)로 받음 (이름이 같아서 자동 매칭)
        return ApiResponse.of(memberService.getMember(memberId));
    }
}
