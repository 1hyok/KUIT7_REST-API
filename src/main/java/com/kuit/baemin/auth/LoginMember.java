package com.kuit.baemin.auth;

import com.kuit.baemin.domain.member.MemberRole;

/**
 * 컨트롤러에 주입되는 '현재 로그인 회원' 정보(불변).
 * Spring Security 의 Jwt(보안 프레임워크 타입)를 컨트롤러에 직접 노출하지 않으려고,
 * 컨트롤러가 실제로 필요한 값(id, role)만 도메인 친화적으로 감싼 작은 객체.
 */
public record LoginMember(Long id, MemberRole role) {

    /** 관리자인지 (소유자 검증을 건너뛰는 전역 권한) */
    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }
}
