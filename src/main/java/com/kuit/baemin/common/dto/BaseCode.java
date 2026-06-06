package com.kuit.baemin.common.dto;

/**
 * 응답 "코드"가 가져야 할 공통 규격(인터페이스).
 * 성공 코드(SuccessStatus)와 에러 코드(ErrorStatus)가 이걸 구현해서, 둘을 같은 방식(getCode/getMessage)으로 다룰 수 있다.
 */
public interface BaseCode {
    String getCode();     // 응답 코드 문자열(예: "20000")
    String getMessage();  // 사용자에게 보여줄 메시지
}
