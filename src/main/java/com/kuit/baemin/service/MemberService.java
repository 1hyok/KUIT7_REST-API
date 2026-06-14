package com.kuit.baemin.service;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.member.Member;
import com.kuit.baemin.dto.request.LoginRequest;
import com.kuit.baemin.dto.request.SignUpRequest;
import com.kuit.baemin.dto.response.MemberResponse;
import com.kuit.baemin.exception.MemberException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import com.kuit.baemin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.INVALID_PASSWORD;
import static com.kuit.baemin.exception.errorcode.ErrorStatus.MEMBER_NOT_FOUND;

/**
 * 회원 관련 '비즈니스 로직'을 담는 Service 계층.
 * Controller(요청 받기)와 Repository(DB 접근) 사이에서, 가입/로그인 같은 '규칙'을 처리한다.
 * (예: 이메일 중복이면 막기, 비밀번호가 틀리면 예외 던지기)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)     // 이 클래스의 모든 메서드는 기본적으로 '읽기 전용 트랜잭션' (조회 성능에 유리, 실수로 DB가 바뀌는 것 방지)
public class MemberService {

    private final MemberRepository memberRepository;   // 위 @RequiredArgsConstructor가 이 필드를 생성자로 주입해 줌

    /**
     * 회원 가입: 이메일 중복을 확인하고, 새 회원을 DB에 저장한 뒤 생성된 id를 돌려준다.
     */
    @Transactional
    public Long signUp(SignUpRequest req) {
        // 이메일 중복 확인: 같은 이메일이 이미 있으면 가입을 막고 예외를 던짐
        if (memberRepository.existsByEmail(req.getEmail())) {
            throw new MemberException(ErrorStatus.DUPLICATE_EMAIL);
        }

        // 요청 DTO(req)의 값들을 빌더로 옮겨 담아 새 Member 엔티티를 조립
        Member member = Member.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .phone(req.getPhone())
                .name(req.getName())
                .status(ActiveStatus.ACTIVE)   // 가입 시점엔 항상 '활성' 상태로 시작 (소프트 삭제 전이라 INACTIVE 아님)
                .build();


        // save()는 MemberRepository에 우리가 안 적었음 — JpaRepository를 상속해 '물려받은' 메서드(인터페이스라 선언만 존재).
        // 실제 구현 코드는 Spring Data JPA가 런타임에 만든 프록시(SimpleJpaRepository)가 제공 → 내부에서 EntityManager.persist 호출 → INSERT.
        Member saved = memberRepository.save(member);   // DB에 INSERT 실행 → PK(id)가 채워진 엔티티를 돌려받음
        return saved.getId();                            // 새로 발급된 회원 id 반환
    }

    /**
     * 로그인: 이메일+활성상태로 회원을 찾고 비밀번호가 맞으면 회원 id를 돌려준다.
     */
    public Long login(LoginRequest req) {
        Member member = memberRepository
                // 이메일이 같아도 status가 ACTIVE인 회원만 조회 (탈퇴/숨김 처리된 INACTIVE 회원은 로그인 불가)
                .findByEmailAndStatus(req.getEmail(), ActiveStatus.ACTIVE)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));   // 결과가 비어 있으면(Optional empty) 예외를 던짐

        // 평문 비교: 저장된 비밀번호와 입력값이 다르면 예외 (실무에선 보통 암호화 후 비교)
        if (!member.getPassword().equals(req.getPassword())) {
            throw new MemberException(INVALID_PASSWORD);
        }

        return member.getId();
    }

    /**
     * 회원 단건 조회: id로 회원을 찾아 응답 DTO로 변환해 돌려준다.
     */
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)              // JpaRepository 기본 제공 메서드 → Optional<Member> 반환 (있을 수도/없을 수도)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));   // orElseThrow: 값이 있으면 그 Member를 꺼내 반환, 비어 있으면(Optional empty) 람다가 만든 예외를 던짐
        return MemberResponse.from(member);   // 엔티티 → 응답 DTO 변환 (password 등 민감 필드는 빼고 내려보냄)
    }
}
