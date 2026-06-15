package com.kuit.baemin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 토큰 재발급 요청 DTO. 클라이언트가 보관 중인 refreshToken을 본문(JSON)으로 보낸다.
 * 컨트롤러가 @RequestBody @Valid로 받아 검증한 뒤 AuthService.reissue에 전달한다.
 */
@Getter
public class ReissueRequest {

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}
