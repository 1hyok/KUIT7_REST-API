package com.kuit.baemin.common.dto;

import static com.kuit.baemin.common.dto.SuccessStatus.API_SUCCESS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모든 API 응답을 동일한 형태로 감싸는 공통 "봉투". <T> = 실제 데이터(result)의 타입.
 * 컨트롤러가 ApiResponse.of(...)로 감싸 반환하면, 클라이언트는 항상 {isSuccess, code, message, result} 구조를 받는다.
 */
@Getter                 // (Lombok) 모든 필드의 getter 자동 생성 (스프링이 이 객체를 JSON으로 바꿀 때 값을 읽어감)
@AllArgsConstructor     // (Lombok) 모든 필드를 받는 생성자 자동 생성 → 아래 static 팩토리의 new ApiResponse<>(...)에서 사용
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})   // JSON으로 나갈 때 필드 순서 고정
public class ApiResponse<T> {

    private final Boolean isSuccess;   // 성공 여부(true/false)
    private final String code;         // 응답 코드(예: "20000")
    private final String message;      // 사람이 읽을 메시지

    @JsonInclude(JsonInclude.Include.NON_NULL)   // result가 null이면 JSON에서 아예 빼고 내보냄
    private T result;                  // 실제 응답 데이터(단건·목록 등)

    // ── 성공 응답 ──
    public static <T> ApiResponse<T> onSuccess(T result) {   // 성공 코드/메시지를 채워 성공 응답 생성
        return new ApiResponse<>(true, API_SUCCESS.getCode(), API_SUCCESS.getMessage(), result);
    }

    public static <T> ApiResponse<T> of(T result) {   // onSuccess와 동일한 짧은 별칭(컨트롤러에서 주로 사용)
        return new ApiResponse<>(true, API_SUCCESS.getCode(), API_SUCCESS.getMessage(), result);
    }

    // ── 실패 응답 ──
    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {   // 실패 코드/메시지로 실패 응답 생성(GlobalExceptionHandler가 사용)
        return new ApiResponse<>(false, code, message, data);
    }
}
