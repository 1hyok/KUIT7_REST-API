package com.kuit.baemin.domain.member;

/**
 * 회원 권한(역할). JWT의 role 클레임 + Spring Security 의 ROLE_ 권한으로 매핑되어 인가에 쓰인다.
 * (예: 가게·메뉴 등록, 주문 상태변경은 OWNER/ADMIN 만 허용)
 */
public enum MemberRole {
    CONSUMER,   // 일반 소비자 — 주문/배송지 등 본인 자원
    OWNER,      // 점주 — 가게·메뉴 등록, 주문 상태변경
    ADMIN       // 관리자 — 전체
}
