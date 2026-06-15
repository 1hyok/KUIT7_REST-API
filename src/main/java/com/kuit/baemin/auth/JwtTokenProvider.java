package com.kuit.baemin.auth;

import com.kuit.baemin.config.JwtProperties;
import com.kuit.baemin.domain.member.MemberRole;
import com.kuit.baemin.exception.AuthException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import lombok.Getter;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.UUID;

/**
 * JWT '발급/해석' 전담 컴포넌트.
 * SecurityConfig가 만든 JwtEncoder(서명)·JwtDecoder(검증)를 받아 access/refresh 토큰을 만들고(create...),
 * refresh 토큰을 검증해 회원 id를 돌려준다(parseRefreshTokenMemberId).
 * Spring Security 의 Jwt 타입은 이 클래스 안에만 두고(private), 바깥(AuthService)엔 Long 만 내보낸다.
 */
@Component
public class JwtTokenProvider {

    // 커스텀 클레임의 '키'들. (SecurityConfig도 이 키로 type/role 을 읽으므로 public). 값은 type=TokenType.claimValue(), role=MemberRole.name()
    public static final String TOKEN_TYPE_CLAIM = "type";   // access/refresh 구분
    public static final String ROLE_CLAIM = "role";         // JwtAuthenticationConverter가 ROLE_<값> 권한으로 매핑 → hasRole 인가에 사용

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshTokenDecoder;   // refresh 검증 전용(서명+만료만, type 무관)
    @Getter private final long accessTokenValiditySeconds;   // @Getter: getAccessTokenValiditySeconds() 자동 생성 (수동 getter 대체)
    @Getter private final long refreshTokenValiditySeconds;

    public JwtTokenProvider(JwtEncoder jwtEncoder,
                            SecretKey secretKey,
                            // 토큰 수명 설정을 JwtProperties(@ConfigurationProperties)에서 주입 (흩어진 @Value 대신 그룹 바인딩)
                            JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        // refresh 토큰 파싱용 디코더(기본 검증=서명+만료). 리소스 서버 디코더는 type=access만 통과시키므로,
        // refresh(type=refresh)를 검증하려면 type 제약이 없는 별도 디코더가 필요하다.
        this.refreshTokenDecoder = NimbusJwtDecoder.withSecretKey(secretKey)   // withSecretKey: '대칭 비밀키(HMAC)'로 검증하는 디코더를 만든다 (RSA 공개키 방식 대신)
                .macAlgorithm(MacAlgorithm.HS256)                              // macAlgorithm: 서명 알고리즘 지정 = HMAC-SHA256(HS256). 발급 때와 같아야 검증 통과
                .build();
        this.accessTokenValiditySeconds = jwtProperties.accessTokenValiditySeconds();
        this.refreshTokenValiditySeconds = jwtProperties.refreshTokenValiditySeconds();
    }

    /** API 접근용 access 토큰 발급 (짧은 수명). 회원의 role 을 클레임으로 실어 인가에 사용 */
    public String createAccessToken(Long memberId, MemberRole role) {
        JwtClaimsSet claims = baseClaims(memberId, TokenType.ACCESS, accessTokenValiditySeconds)
                .claim(ROLE_CLAIM, role.name())   // access 에만 권한 클레임(예: "OWNER") 추가
                .build();
        return encode(claims);
    }

    /** access 만료 시 재발급에 쓰는 refresh 토큰 발급 (긴 수명, Redis에 보관). 권한은 안 실음 — 재발급 때 DB에서 다시 조회 */
    public String createRefreshToken(Long memberId) {
        return encode(baseClaims(memberId, TokenType.REFRESH, refreshTokenValiditySeconds).build());
    }

    /** 모든 토큰 공통 클레임(jti·sub·iat·exp·type)을 채운 빌더 반환. role 처럼 토큰별로 다른 건 호출부에서 추가 → null 파라미터 불필요 */
    private JwtClaimsSet.Builder baseClaims(Long memberId, TokenType type, long validitySeconds) {
        Instant now = Instant.now();   // 지금 시각(UTC 시점). iat=now(발급), exp=now+수명(만료)
        return JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())              // jti = 토큰 고유 id (같은 초 발급에도 매번 달라져 회전·재사용 탐지가 동작)
                .subject(String.valueOf(memberId))             // sub = 회원 식별자
                .issuedAt(now)                                 // iat = 발급 시각
                .expiresAt(now.plusSeconds(validitySeconds))   // exp = 만료 시각
                .claim(TOKEN_TYPE_CLAIM, type.claimValue());   // type = "access"/"refresh" (TokenType이 값 보유)
    }

    /** 클레임을 HS256으로 서명해 토큰 문자열로 인코딩 */
    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();   // 헤더 alg=HS256 ("HS256으로 서명됨")
        // from(header,claims) = 둘을 인코더 입력으로 묶음 → encode()가 Base64Url 인코딩 + HS256 서명해서 'header.payload.signature' 토큰을 생성 → getTokenValue()로 문자열 추출
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * refresh 토큰 문자열을 검증(서명+만료 + type=refresh)하고 회원 id(Long)를 돌려준다.
     * Jwt(Spring Security 타입)를 이 메서드 안에서만 다뤄, 호출자(AuthService)는 Long 만 받게 한다.
     */
    public Long parseRefreshTokenMemberId(String refreshToken) {
        Jwt jwt = parse(refreshToken);     // 서명+만료 검증 (실패 시 AuthException)
        if (!isRefreshToken(jwt)) {        // access 토큰을 재발급에 쓰는 것 차단
            throw new AuthException(ErrorStatus.INVALID_TOKEN);
        }
        return getMemberId(jwt);
    }

    // ── 아래는 내부 전용(private). Jwt 타입이 이 클래스 밖으로 새어 나가지 않게 캡슐화 ──

    /** 토큰 검증(서명+만료) 후 Jwt 로 파싱. 실패하면 우리 AuthException 으로 변환 */
    private Jwt parse(String token) {
        try {
            return refreshTokenDecoder.decode(token);
        } catch (JwtValidationException e) {
            throw new AuthException(ErrorStatus.EXPIRED_TOKEN);   // 서명은 맞지만 만료(exp) 등 검증 위반
        } catch (JwtException e) {
            throw new AuthException(ErrorStatus.INVALID_TOKEN);   // 서명 위조·형식 오류 등
        }
    }

    private Long getMemberId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());   // 토큰의 sub(문자열) → 회원 id(Long)
    }

    private boolean isRefreshToken(Jwt jwt) {
        return TokenType.from(jwt.getClaimAsString(TOKEN_TYPE_CLAIM)) == TokenType.REFRESH;   // 토큰 type 클레임 → enum 변환 후 enum 비교(문자열 .equals 대신)
    }
}
