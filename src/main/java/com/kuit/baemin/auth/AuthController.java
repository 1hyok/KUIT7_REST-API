package com.kuit.baemin.auth;

import com.kuit.baemin.auth.dto.ReissueRequest;
import com.kuit.baemin.auth.dto.TokenResponse;
import com.kuit.baemin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API. (로그인은 기존대로 /members/login 에 두고, 여기선 토큰 재발급/로그아웃을 담당)
 */
@Tag(name = "Auth", description = "인증 API (토큰 재발급/로그아웃)")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "토큰 재발급 (refreshToken으로 access/refresh 회전 발급)")
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@Valid @RequestBody ReissueRequest req) {
        return ApiResponse.of(authService.reissue(req.getRefreshToken()));
    }

    @Operation(summary = "로그아웃 (서버에 저장된 refreshToken 삭제)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Login LoginMember member) {
        // @Login LoginMember : 우리가 만든 커스텀 ArgumentResolver가 검증된 토큰에서 꺼낸 '현재 로그인 회원' 정보를 주입.
        //   컨트롤러는 더 이상 Spring Security 의 Jwt 타입을 직접 만지지 않는다(디커플링).
        authService.logout(member.id());   // 로그인 회원의 refresh 폐기
        return ApiResponse.success();   // 돌려줄 데이터 없는 성공
    }
}
