package com.kuit.baemin.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인가 실패 처리기 — HTTP 403.
 * 인증은 됐지만(토큰은 유효) 그 리소스에 접근할 '권한'이 없을 때 호출된다.
 * 역시 공통 {@link ApiResponse} JSON 형식으로 통일해 내려준다.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(ErrorStatus.FORBIDDEN.getHttpStatus().value());        // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> body = ApiResponse.onFailure(
                ErrorStatus.FORBIDDEN.getCode(), ErrorStatus.FORBIDDEN.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
