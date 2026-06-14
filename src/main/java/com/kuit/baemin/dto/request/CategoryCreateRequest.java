package com.kuit.baemin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 카테고리 생성 요청 DTO.
 * 클라이언트가 보낸 JSON 요청 본문(request body)을 담아 컨트롤러로 전달하는 객체.
 * 컨트롤러 파라미터에 @Valid 를 붙이면 아래 검증 규칙이 자동으로 실행되고,
 * 규칙을 어기면 요청은 컨트롤러 로직에 도달하기 전에 예외로 막힌다.
 * ※ '검증 규칙'은 바로 아래 필드에 붙은 @NotBlank/@Size 같은 애너테이션이며, 개발자(우리)가 직접 골라 붙인다.
 *   (@NotBlank 등은 Jakarta Validation 이 종류를 제공, 실제 검사는 Hibernate Validator 가 수행, @Valid 는 '실행하라' 스위치일 뿐)
 */
@Getter
public class CategoryCreateRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다.")            // 값이 null/빈 문자열/공백만 있으면 실패(문자열 전용). message=실패 시 응답에 담길 안내문(미지정 시 기본 영어 메시지)
    @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.") // 길이 제한(문자열 글자 수). 최대 50자까지만 허용. message=실패 시 안내문
    private String name;
}
