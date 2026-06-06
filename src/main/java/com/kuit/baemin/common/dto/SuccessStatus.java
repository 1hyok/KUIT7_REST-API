package com.kuit.baemin.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 성공 응답에 쓰는 코드 모음(enum). BaseCode를 구현해 ApiResponse가 코드/메시지를 꺼내 쓴다. (실패용은 ErrorStatus)
 */
@Getter                 // (Lombok) 모든 필드(isSuccess/code/message)의 getter 자동 생성
@AllArgsConstructor     // (Lombok) 모든 필드를 받는 생성자 자동 생성 → 아래 enum 상수 API_SUCCESS(...) 정의가 이 생성자를 호출
public enum SuccessStatus implements BaseCode {

    // 공통
    API_SUCCESS(true, "20000", "요청에 성공했습니다."),   // 각 상수 = (성공여부, 코드, 메시지)
    ;

    private final boolean isSuccess;
    private final String code;
    private final String message;
}
