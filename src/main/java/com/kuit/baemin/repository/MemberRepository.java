package com.kuit.baemin.repository;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 Repository — DB에 직접 접근하는 계층.
 * 'interface'(설계도)만 선언하면, 실제 구현체는 Spring Data JPA가 실행 시점에 자동으로 만들어 줍니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 아래는 '쿼리 메서드' — 메서드 '이름'만 규칙대로 지으면 JPA가 SQL을 자동으로 만들어 줍니다.
    // Optional = 결과가 없을 수도 있음을 타입으로 표현 (null 대신 비어있는 Optional 반환)

    // findBy + Email + And + Status → WHERE email = ? AND status = ?
    // 두 번째 인자로 받은 status 값으로 거름 (로그인 때 ACTIVE 를 넘겨 소프트 삭제된 INACTIVE 회원을 제외)
    Optional<Member> findByEmailAndStatus(String email, ActiveStatus status);

    // existsBy + Email → SELECT ... WHERE email = ? 의 존재 여부만 boolean으로 반환 (이메일 중복 확인)
    boolean existsByEmail(String email);
}
