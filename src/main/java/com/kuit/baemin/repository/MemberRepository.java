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

    // 아래는 '쿼리 메서드(파생 쿼리)' — 바디를 안 적어도 Spring Data가 메서드 '이름'을 파싱해 쿼리를 자동 생성합니다.
    //   동작: 시작 시 인터페이스의 '프록시(가짜 구현체)'를 만들고, 메서드 이름을 두 부분으로 쪼개 해석한다.
    //     ① 주어(subject): find…By / read…By / get…By / exists…By / count…By / delete…By → 무슨 작업인지 결정
    //     ② 조건(predicate): By 뒤 = 엔티티 '속성명' + 키워드(And/Or/GreaterThan/Like/Between/In/IsNull/OrderBy…)
    //   인자는 By 뒤 조건 순서대로 바인딩되고, 반환 타입도 규칙: exists→boolean, count→long, find→엔티티/Optional/List/Page
    // Optional = 결과가 없을 수도 있음을 타입으로 표현 (null 대신 비어있는 Optional 반환)

    // findBy + Email + And + Status → WHERE email = ? AND status = ?
    // 두 번째 인자로 받은 status 값으로 거름 (로그인 때 ACTIVE 를 넘겨 소프트 삭제된 INACTIVE 회원을 제외)
    Optional<Member> findByEmailAndStatus(String email, ActiveStatus status);

    // existsBy + Email → SELECT ... WHERE email = ? 의 존재 여부만 boolean으로 반환 (이메일 중복 확인)
    boolean existsByEmail(String email);

    // 전화번호 중복 확인 (phone 도 unique 제약이라 가입 전에 미리 검사해 깔끔한 409로 응답)
    boolean existsByPhone(String phone);
}
