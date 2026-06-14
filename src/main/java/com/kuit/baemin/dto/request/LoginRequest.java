package com.kuit.baemin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 로그인 요청 DTO. 컨트롤러가 @RequestBody @Valid로 받아 검증한 뒤 MemberService.login에 전달한다.
 */
@Getter   // (Lombok) getter 자동 생성. 서비스가 값을 꺼내 읽을 때 사용
public class LoginRequest {

    @NotBlank(message = "이메일은 필수입니다.")          // null·빈문자열·공백 모두 불허
    @Email(message = "올바른 이메일 형식이 아닙니다.")   // 이메일 형식(@ 포함 등) 검증
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
