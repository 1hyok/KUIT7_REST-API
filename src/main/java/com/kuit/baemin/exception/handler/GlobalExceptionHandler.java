package com.kuit.baemin.exception.handler;

import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.exception.GeneralException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * 전역 예외 처리기(Exception Handler).
 * 컨트롤러들에서 던져진 예외를 한곳에서 가로채, 클라이언트에게 줄 일관된 에러 응답(JSON)으로 변환하는 책임을 가진다.
 * 각 컨트롤러마다 try-catch를 반복하지 않아도 되도록, 예외 처리를 이 클래스 하나로 모아 둔 것.
 *
 * 동작 흐름: 컨트롤러/서비스에서 예외 발생 → 아래 @ExceptionHandler 중 "그 예외 타입과 가장 잘 맞는" 메서드가 자동 호출 → ApiResponse(실패 형태)로 응답.
 */
@Slf4j                  // Lombok: 이 클래스에서 바로 쓸 수 있는 로거(log) 필드를 자동 생성 (log.warn/log.error 사용 가능)
@RestControllerAdvice   // 모든 @RestController에 공통 적용되는 예외 처리기. 반환값은 @ResponseBody처럼 JSON 본문으로 직렬화됨
public class GlobalExceptionHandler {

    /**
     * 요청 DTO의 @Valid(Bean Validation) 검증 실패 시 호출 — HTTP 400(Bad Request).
     * 예: @NotBlank/@NotNull 등이 깨지면 Spring이 MethodArgumentNotValidException을 던지고, 그것을 여기서 받는다.
     */
    @ResponseStatus(BAD_REQUEST)                                 // 이 핸들러의 응답 상태코드를 400으로 고정 (항상 같은 상태라 ResponseEntity 대신 고정값 사용)
    @ExceptionHandler(MethodArgumentNotValidException.class)     // 이 타입의 예외가 발생하면 이 메서드가 처리하도록 매핑
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {   // 에러 응답은 result가 없으니(null) <Void>로 명시 (와일드카드 <?> 회피)
        // 검증 실패한 필드들의 메시지를 모아 하나의 문자열로 합친다.
        String message = e.getBindingResult()
                .getFieldErrors()                               // 검증에 실패한 필드 목록 (필드별 오류 1건씩)
                .stream()
                .map(FieldError::getDefaultMessage)             // 각 오류 → 사람이 읽을 메시지 (어노테이션의 message 값)
                .collect(Collectors.joining(", "));             // 여러 개면 ", "로 이어 붙임. 예: "이름은 필수입니다, 가격은 0 이상이어야 합니다"
        log.warn("[Validation 오류] {}", message);              // 사용자 입력 실수 수준이라 warn (서버 장애가 아님)
        return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST.getCode(), message);
    }

    /**
     * 우리가 직접 정의해 의도적으로 던지는 비즈니스 예외(GeneralException) 처리.
     * GeneralException은 ErrorStatus(코드+메시지+HTTP상태)를 들고 다니므로, 그 안의 상태를 그대로 응답에 반영한다.
     * 상태코드가 예외(ErrorStatus)마다 달라지므로(404/409/403 등), 고정 @ResponseStatus 대신 ResponseEntity로 동적으로 지정한다.
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {   // 에러 응답은 result 없음 → <Void> (와일드카드 <?> 회피)
        ErrorStatus errorStatus = e.getErrorStatus();           // 예외가 품은 에러 정의(우리 도메인). HTTP status와 헷갈리지 않게 errorStatus로 명명
        log.warn("[GeneralException] code={}, message={}", errorStatus.getCode(), errorStatus.getMessage());
        return ResponseEntity
                .status(errorStatus.getHttpStatus())            // ResponseEntity.status(...) = 'HTTP 상태' 설정(Spring 메서드). errorStatus가 정한 상태코드로 응답 (예: NOT_FOUND→404)
                .body(ApiResponse.onFailure(errorStatus.getCode(), errorStatus.getMessage()));   // 본문은 일관된 실패 응답 형태
    }

    /**
     * 위에서 잡지 못한 그 외 모든 예상치 못한 예외 — HTTP 500(Internal Server Error).
     * Exception을 받으므로 위 핸들러들에 매칭되지 않은 예외의 "최후 방어선" 역할 (NPE 등 버그성 예외).
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {   // 에러 응답은 result 없음 → <Void> (와일드카드 <?> 회피)
        log.error("[Exception] 처리되지 않은 예외 발생", e);    // 예상 못 한 서버 버그이므로 error 레벨 + 예외 객체(e)를 넘겨 스택트레이스까지 로그로 남김
        return ApiResponse.onFailure(
                ErrorStatus.INTERNAL_SERVER_ERROR.getCode(),    // 내부 원인은 로그에만 남기고, 응답엔 일반화된 메시지만 노출 (보안상 상세 노출 X)
                ErrorStatus.INTERNAL_SERVER_ERROR.getMessage()
        );
    }
}
