package com.kuit.baemin.auth;

import com.kuit.baemin.auth.dto.TokenResponse;
import com.kuit.baemin.domain.member.Member;
import com.kuit.baemin.domain.member.MemberRole;
import com.kuit.baemin.dto.request.LoginRequest;
import com.kuit.baemin.exception.AuthException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import com.kuit.baemin.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증(토큰) 오케스트레이션 서비스. "자격 확인 → 토큰 발급 → refresh 저장/회전"의 흐름을 담당한다.
 * (이메일/비밀번호 확인 자체는 MemberService에 위임하고, 여기선 토큰 발급·저장에 집중)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberService memberService;                  // 이메일+비밀번호(BCrypt) 검증 재사용
    private final JwtTokenProvider jwtTokenProvider;            // 토큰 발급/해석
    private final RefreshTokenRepository refreshTokenRepository; // refresh 보관(Redis)

    /** 로그인: 자격 확인 → access/refresh 발급 → refresh를 Redis에 저장 → 토큰 응답 */
    public TokenResponse login(LoginRequest req) {
        Member member = memberService.login(req);   // 실패하면 MemberService가 예외(MEMBER_NOT_FOUND / INVALID_PASSWORD)
        return issueAndStore(member.getId(), member.getRole());
    }

    /**
     * 재발급(회전): 받은 refresh가 (1)유효한 refresh 토큰이고 (2)Redis에 저장된 값과 일치하면
     * 새 access+refresh를 발급하고 Redis의 refresh를 새 값으로 교체(회전)한다.
     * 저장된 값과 다르면 = 이미 회전돼 폐기된 옛 토큰의 재사용 → 탈취로 간주해 통째로 무효화(재사용 탐지).
     *
     * <p>한계(의도된 단순화): 만료(exp 경과)된 refresh는 parse 단계에서 EXPIRED로 먼저 거부되므로,
     * '만료된 옛 토큰의 재사용'까지는 강제 무효화가 발동하지 않는다. 다만 만료된 토큰으로는 어차피 재발급이
     * 불가능하므로(새 토큰을 못 얻음) 실제 피해는 없고, '정상 사용자 세션 강제 로그아웃' 신호만 생략될 뿐이다.
     * 또한 findById→비교→save 가 한 트랜잭션으로 원자화돼 있지 않아, 동시 재발급 시 드물게 경합 여지가 있다.
     */
    public TokenResponse reissue(String refreshToken) {
        // 검증(서명+만료+refresh 타입)과 memberId 추출을 JwtTokenProvider가 캡슐화 → 여기선 Jwt를 보지 않고 Long만 다룸
        Long memberId = jwtTokenProvider.parseRefreshTokenMemberId(refreshToken);

        RefreshToken stored = refreshTokenRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND));  // 로그아웃/만료로 Redis에 없음

        if (!stored.getToken().equals(refreshToken)) {
            // 저장된 것과 다른 refresh = 폐기된 옛 토큰 재사용 의심 → 해당 회원 refresh 전체 폐기(재로그인 강제)
            refreshTokenRepository.deleteById(memberId);
            throw new AuthException(ErrorStatus.REFRESH_TOKEN_MISMATCH);
        }
        MemberRole role = memberService.getRole(memberId);   // 새 access 토큰에 실을 현재 권한을 DB에서 다시 조회
        return issueAndStore(memberId, role);                // 회전: 새 refresh로 교체 저장
    }

    /** 로그아웃: Redis의 refresh 삭제 → 이후 재발급 불가. (access는 만료까지 유효하므로 수명을 짧게 둠) */
    public void logout(Long memberId) {
        refreshTokenRepository.deleteById(memberId);
    }

    /** issueAndStore = issue(발급)+store(저장). issue=access·refresh '둘 다' 발급, store=그중 refresh만 Redis에 저장(회전 시 교체) → 토큰 응답 생성 */
    private TokenResponse issueAndStore(Long memberId, MemberRole role) {
        String accessToken = jwtTokenProvider.createAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);
        refreshTokenRepository.save(new RefreshToken(
                memberId, refreshToken, jwtTokenProvider.getRefreshTokenValiditySeconds()));
        return TokenResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenValiditySeconds());
    }
}
