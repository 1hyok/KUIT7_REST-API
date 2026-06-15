package com.kuit.baemin.exception;

import com.kuit.baemin.exception.errorcode.ErrorStatus;

/**
 * 인증/인가(JWT) 전용 예외.
 * MemberException과 마찬가지로 GeneralException을 상속하므로, 던지면 GlobalExceptionHandler가
 * 받아서 ErrorStatus의 코드·메시지로 통일된 JSON 에러 응답으로 변환한다.
 */
public class AuthException extends GeneralException {
    public AuthException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
