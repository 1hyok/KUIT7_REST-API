package com.kuit.baemin.exception;

import com.kuit.baemin.exception.errorcode.ErrorStatus;

/** 회원(Member) 도메인 전용 예외. (예외→응답 변환 등 공통 흐름은 GeneralException 참고) */
public class MemberException extends GeneralException {
    public MemberException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
