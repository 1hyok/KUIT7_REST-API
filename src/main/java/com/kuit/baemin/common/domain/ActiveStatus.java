package com.kuit.baemin.common.domain;

/**
 * 소프트 삭제용 공통 상태값을 정의한 enum.
 *
 * <p>"소프트 삭제(soft delete)"란 행을 DB에서 진짜 DELETE 하지 않고,
 * 상태 컬럼을 INACTIVE 로 바꿔 "삭제된 것처럼" 숨기는 방식이다.
 *
 * <p>여러 엔티티(회원/식당/메뉴 등)가 공통으로 쓰기 때문에 common 패키지에 둔다.
 * 각 엔티티는 이 enum 타입의 필드를 가지며, DB에는 소문자 문자열("active"/"inactive")로 저장된다.
 * 이 변환(자바 enum ↔ DB 소문자 문자열)은 ActiveStatusConverter(@Converter(autoApply=true))가 자동으로 처리한다.
 * (ERD의 status ENUM('active','inactive')에 맞추려고 @Enumerated 대신 컨버터를 쓴다 —
 *  그건 @Enumerated(EnumType.STRING)이 상수 이름을 대문자 "ACTIVE" 그대로 저장하기 때문)
 */
public enum ActiveStatus {
    ACTIVE,    // 활성
    INACTIVE   // 비활성(삭제/숨김)
}
