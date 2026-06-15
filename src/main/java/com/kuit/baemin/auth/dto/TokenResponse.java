package com.kuit.baemin.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인/재발급 성공 시 클라이언트에 내려주는 토큰 묶음.
 * 클라이언트는 accessToken을 보관했다가 요청 헤더에 {@code Authorization: Bearer <accessToken>}로 실어 보낸다.
 */
@Getter
@Builder
public class TokenResponse {

    private String grantType;            // 토큰 사용 방식. 항상 "Bearer"
    private String accessToken;          // API 접근용 (짧은 수명)
    private String refreshToken;         // access 만료 시 재발급용 (긴 수명)
    private long accessTokenExpiresIn;   // accessToken 유효시간(초) — 클라이언트가 만료 시점 가늠용

    public static TokenResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }
}
