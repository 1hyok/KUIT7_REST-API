package com.kuit.baemin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증 실패(토큰 없음/만료/위조) 처리기 — HTTP 401.
 * Spring Security 기본 응답 대신, 이 프로젝트 공통 {@link ApiResponse} JSON 형식으로 통일해 내려준다.
 * (이 단계는 컨트롤러에 닿기 전 '필터'에서 막히므로 GlobalExceptionHandler가 못 잡음 → 여기서 직접 응답 작성)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;   // 객체 → JSON 직렬화 (Spring Boot가 자동 등록한 빈)

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(ErrorStatus.UNAUTHORIZED.getHttpStatus().value());     // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);   // 응답 Content-Type 헤더를 "application/json"으로 (본문이 JSON임을 알림). _VALUE = 그 MIME 타입의 '문자열' 형태
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());   // 응답 본문을 UTF-8로 인코딩 (한글 에러 메시지 안 깨지게). .name()=Charset.name(): charset의 정식 이름 문자열("UTF-8") 반환 (Servlet 6.1엔 Charset 그대로 받는 오버로드도 있어 .name() 생략 가능)
        ApiResponse<Void> body = ApiResponse.onFailure(
                ErrorStatus.UNAUTHORIZED.getCode(), ErrorStatus.UNAUTHORIZED.getMessage());
        // writeValueAsString: ApiResponse 객체 → JSON 문자열(Jackson 직렬화) / getWriter().write: 그 문자열을 응답 본문에 써넣음 (@RestController의 자동 JSON 변환을 여기선 수동으로)
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
