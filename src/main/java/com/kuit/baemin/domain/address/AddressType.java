package com.kuit.baemin.domain.address;

/**
 * 배송지 종류 (ERD: address.type) — Address 엔티티의 type 필드로 쓰인다.
 */
public enum AddressType {
    HOME,   // 집
    WORK,   // 회사
    ETC     // 기타
}
