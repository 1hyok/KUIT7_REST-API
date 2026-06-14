package com.kuit.baemin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 회원가입 요청 DTO.
 * 클라이언트가 보낸 회원가입 JSON 본문을 담는 그릇으로, 컨트롤러로 들어올 때 이 객체로 변환된다.
 */
@Getter   // 필드 getter 자동 생성. 요청을 역직렬화하거나 서비스에서 값을 꺼낼 때 사용
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")        // null, 빈 문자열, 공백만 있는 값 모두 거부 (문자열 전용)
    @Email(message = "올바른 이메일 형식이 아닙니다.")   // 이메일 형식(...@...) 검증
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")   // 글자 수가 8~20 범위인지 검증
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")   // 최대 길이만 제한 (최소는 NotBlank 가 보장)
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    // 정규식으로 전화번호 형식 검증. 010/011/016/017/018/019로 시작, 가운데 3~4자리, 끝 4자리 (하이픈 포함)
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. ex) 010-1234-5678")
    private String phone;
}
