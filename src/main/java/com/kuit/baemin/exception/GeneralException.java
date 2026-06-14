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
        super(errorStatus.getMessage());   // 이 문자열 = 예외의 '상세 메시지(detail message)'. Throwable에 저장돼 getMessage()로 꺼내지고, 스택트레이스/로그에 "GeneralException: <이 문구>" 형태로 찍힘 (동작엔 영향 없고 '무슨 에러인지' 사람이 읽는 설명용)
        this.errorStatus = errorStatus;
    }
}
