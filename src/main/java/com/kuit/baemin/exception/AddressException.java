package com.kuit.baemin.exception;

import com.kuit.baemin.exception.errorcode.ErrorStatus;

/** 배송지(Address) 도메인 전용 예외. (예외→응답 변환 등 공통 흐름은 GeneralException 참고) */
public class AddressException extends GeneralException {
    public AddressException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
