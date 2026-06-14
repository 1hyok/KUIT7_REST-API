package com.kuit.baemin.exception;

import com.kuit.baemin.exception.errorcode.ErrorStatus;
import lombok.Getter;

/**
 * 이 프로젝트 모든 도메인 예외(MemberException 등)의 공통 부모.
 * RuntimeException을 상속해 try-catch 강제 없이 던질 수 있고, "어떤 에러인지"(ErrorStatus)를 함께 들고 다닌다.
 * 던져진 예외는 GlobalExceptionHandler가 받아 ErrorStatus의 코드·메시지로 통일된 JSON 에러 응답으로 변환한다.
 */
@Getter
public class GeneralException extends RuntimeException {

    private final ErrorStatus errorStatus;   // 무슨 에러인지(코드·메시지 등)를 담은 값

    public GeneralException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());   // 부모(RuntimeException)의 메시지로 ErrorStatus 메시지를 전달(로그 등에 표시)
        this.errorStatus = errorStatus;
    }
}
