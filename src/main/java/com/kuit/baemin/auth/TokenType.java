package com.kuit.baemin.auth;

/**
 * JWT 토큰 종류. JWT의 "type" 클레임 값(access/refresh)으로 박혀, 발급/검증에서 토큰 용도를 구분한다.
 * (boolean 대신 enum — 호출부가 자기설명적이고, 타입 안전하며, 나중에 종류를 늘리기도 쉽다)
 */
public enum TokenType {

    ACCESS("access"),     // API 접근용 (짧은 수명, role 클레임 포함)
    REFRESH("refresh");   // 재발급용 (긴 수명, Redis 보관, role 없음)

    private final String claimValue;   // JWT "type" 클레임에 실제로 들어갈 문자열

    TokenType(String claimValue) {
        this.claimValue = claimValue;
    }

    public String claimValue() {
        return claimValue;
    }

    /** 클레임 문자열("access"/"refresh")을 TokenType으로 변환. 알 수 없는 값이나 null이면 null 반환(→ 비교 시 안전하게 불일치 처리) */
    public static TokenType from(String claimValue) {
        for (TokenType type : values()) {
            if (type.claimValue.equals(claimValue)) {
                return type;
            }
        }
        return null;
    }
}
