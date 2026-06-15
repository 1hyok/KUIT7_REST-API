package com.kuit.baemin.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * jwt.* 외부 설정을 한 곳에 타입세이프하게 묶는 설정 프로퍼티(record).
 *
 * <p>흩어진 {@code @Value} 대신, 접두사로 묶인 그룹 설정을 {@code @ConfigurationProperties}로 바인딩하고
 * {@code @Validated}로 애플리케이션 기동 시 검증한다. (Spring Boot 권장: 여러 개/계층적 설정은 @Value보다 이 방식)
 *
 * <p>등록은 {@code BaeminApplication}의 {@code @EnableConfigurationProperties(JwtProperties.class)}로 한다.
 * record라 생성자 바인딩으로 채워진다(주입 시점에 1회).
 */
@Validated   // 아래 필드의 검증 규칙(@NotBlank/@Positive)을 바인딩 시 실제로 검사 → 위반이면 앱 기동 실패(설정 실수 조기 발견). 없으면 규칙이 붙어 있어도 검사 안 함
@ConfigurationProperties(prefix = "jwt")   // jwt.secret / jwt.access-token-validity-seconds / jwt.refresh-token-validity-seconds 바인딩 (케밥케이스→카멜케이스 완화 바인딩)
public record JwtProperties(
        @NotBlank String secret,                     // HS256 서명 비밀키(영문 32자=256비트 이상). 비어 있으면 기동 실패 → 설정 누락을 조기 발견
        @Positive long accessTokenValiditySeconds,   // access 토큰 수명(초). 0 이하면 기동 실패
        @Positive long refreshTokenValiditySeconds   // refresh 토큰 수명(초). 0 이하면 기동 실패
) {
}
