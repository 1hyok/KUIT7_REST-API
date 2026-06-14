package com.kuit.baemin.exception;

import com.kuit.baemin.exception.errorcode.ErrorStatus;

/** 가게(Restaurant) 도메인 전용 예외. (예외→응답 변환 등 공통 흐름은 GeneralException 참고) */
public class RestaurantException extends GeneralException {
    public RestaurantException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
