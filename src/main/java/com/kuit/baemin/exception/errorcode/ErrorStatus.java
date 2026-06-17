package com.kuit.baemin.exception.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 이 프로젝트에서 발생할 수 있는 "에러의 종류"를 한 곳에 모아 둔 enum 이다.
 *
 * <p>각 상수(BAD_REQUEST, MEMBER_NOT_FOUND ...)는 하나의 에러 상황을 뜻하며,
 * 세 가지 정보를 묶어서 들고 있다.
 * <ul>
 *   <li>httpStatus : 응답으로 내려줄 HTTP 상태 코드 (404, 409 ...)</li>
 *   <li>code       : 우리 서비스가 직접 정한 '의미형' 식별 문자열 ("MEMBER_NOT_FOUND" 등). HTTP 상태코드와 헷갈리지 않게 일부러 숫자가 아닌 '이름'을 쓴다. 프런트가 세부 분기할 때 사용</li>
 *   <li>message    : 사용자/개발자에게 보여줄 한국어 설명</li>
 * </ul>
 *
 * <p>보통 비즈니스 로직에서 잘못된 상황을 만나면 이 enum 값을 골라 커스텀 예외에 담아 던지고,
 * 전역 예외 처리기(@RestControllerAdvice 등)가 이 값을 읽어 일관된 형식의 에러 응답으로 변환한다.
 * 에러를 한 곳(enum)에서 관리하므로, 새 에러가 필요하면 여기에 한 줄만 추가하면 된다.
 */
@Getter                 // Lombok: 아래 3개 필드의 getter(getHttpStatus/getCode/getMessage)를 자동 생성
@AllArgsConstructor
public enum ErrorStatus {

    // 아래 상수들은 도메인별로 묶어 두었다. (HttpStatus, "코드", "메시지") 순으로 생성자 인자를 넘긴다.
    // code = enum 상수 '이름' 그대로(의미형). HTTP status 와 헷갈리지 않게 숫자를 쓰지 않는다.
    // ── 공통 ──
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    // ── 회원 ──
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "DUPLICATE_PHONE", "이미 사용 중인 전화번호입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),

    // ── 카테고리 ──
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "존재하지 않는 카테고리입니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "DUPLICATE_CATEGORY_NAME", "이미 존재하는 카테고리 이름입니다."),

    // ── 가게 ──
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "RESTAURANT_NOT_FOUND", "존재하지 않는 가게입니다."),
    RESTAURANT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "RESTAURANT_NOT_ACTIVE", "현재 주문할 수 없는 가게입니다."),
    RESTAURANT_FORBIDDEN(HttpStatus.FORBIDDEN, "RESTAURANT_FORBIDDEN", "본인 가게가 아닙니다."),

    // ── 메뉴 ──
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "존재하지 않는 메뉴입니다."),
    MENU_NOT_IN_RESTAURANT(HttpStatus.BAD_REQUEST, "MENU_NOT_IN_RESTAURANT", "해당 가게의 메뉴가 아닙니다."),
    MENU_NOT_ORDERABLE(HttpStatus.BAD_REQUEST, "MENU_NOT_ORDERABLE", "현재 주문할 수 없는 메뉴입니다."),

    // ── 옵션 ──
    MENU_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_OPTION_NOT_FOUND", "존재하지 않는 옵션입니다."),
    INVALID_MENU_OPTION(HttpStatus.BAD_REQUEST, "INVALID_MENU_OPTION", "해당 메뉴의 옵션이 아닙니다."),
    REQUIRED_OPTION_GROUP_MISSING(HttpStatus.BAD_REQUEST, "REQUIRED_OPTION_GROUP_MISSING", "필수 옵션 그룹을 선택하지 않았습니다."),
    OPTION_GROUP_SINGLE_VIOLATED(HttpStatus.BAD_REQUEST, "OPTION_GROUP_SINGLE_VIOLATED", "단일 선택 옵션 그룹에서는 옵션을 하나만 선택할 수 있습니다."),

    // ── 배송지 ──
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "존재하지 않는 배송지입니다."),
    ADDRESS_FORBIDDEN(HttpStatus.FORBIDDEN, "ADDRESS_FORBIDDEN", "본인의 배송지가 아닙니다."),

    // ── 주문 ──
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "존재하지 않는 주문입니다."),
    ORDER_BELOW_MIN_PRICE(HttpStatus.BAD_REQUEST, "ORDER_BELOW_MIN_PRICE", "최소 주문 금액 미만입니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "ORDER_NOT_CANCELABLE", "취소할 수 없는 주문 상태입니다."),
    ORDER_STATUS_NOT_CHANGEABLE(HttpStatus.BAD_REQUEST, "ORDER_STATUS_NOT_CHANGEABLE", "이미 종료된 주문은 상태를 변경할 수 없습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "ORDER_FORBIDDEN", "주문에 대한 권한이 없습니다."),

    // ── 인증/인가 (JWT) ──
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND", "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_MISMATCH", "재사용이 감지된 토큰입니다. 보안을 위해 다시 로그인해 주세요."),
    ;

    // 위 각 상수가 들고 있는 값들. final 이라 한번 정해지면 바뀌지 않는다(enum 상수의 고정된 속성).
    private final HttpStatus httpStatus;    // 응답에 쓸 HTTP 상태 코드
    private final String code;              // 서비스 자체 에러 식별 코드(의미형 이름. HTTP status 와 별개라 숫자 대신 이름 사용)
    private final String message;           // 에러 설명 메시지
}
