package com.kuit.baemin.service;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.address.Address;
import com.kuit.baemin.domain.member.Member;
import com.kuit.baemin.dto.request.AddressCreateRequest;
import com.kuit.baemin.dto.response.AddressResponse;
import com.kuit.baemin.exception.MemberException;
import com.kuit.baemin.repository.AddressRepository;
import com.kuit.baemin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.MEMBER_NOT_FOUND;

/**
 * 배송지(Address) 관련 비즈니스 로직을 담당하는 서비스 계층.
 * Controller(요청 받기)와 Repository(DB 접근) 사이에서 "무엇을 어떻게 처리할지"를 결정한다.
 * 예: 회원이 진짜 존재하는지 검증한 뒤 배송지를 저장하거나, 회원의 활성 배송지만 골라 응답으로 변환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)           // 클래스 전체 기본값: 읽기 전용 트랜잭션 (조회 성능 최적화, 쓰기 작업은 막힘)
public class AddressService {

    // 생성자 주입 대상. final + @RequiredArgsConstructor 조합으로 스프링이 자동으로 채워준다.
    private final AddressRepository addressRepository;   // 배송지 DB 접근
    private final MemberRepository memberRepository;     // 회원 존재 검증용 DB 접근

    /**
     * 배송지 등록. 회원을 찾아 그 회원 소유의 새 배송지를 저장하고, 생성된 배송지의 PK를 돌려준다.
     */
    @Transactional
    public Long create(Long memberId, AddressCreateRequest req) {
        // 회원을 PK로 조회. 없으면 Optional 이 비어있고, orElseThrow 가 예외를 던져 메서드를 중단시킨다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

        // 빌더 패턴으로 Address 엔티티 생성. 요청 DTO(req)의 값과 조회한 member 를 조립한다.
        Address address = Address.builder()
                .member(member)
                .type(req.getType())
                .alias(req.getAlias())
                .roadAddress(req.getRoadAddress())
                .detailAddress(req.getDetailAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(ActiveStatus.ACTIVE)   // 새로 만든 배송지는 기본적으로 '활성' 상태로 시작
                .build();

        // save 로 DB에 INSERT 한 뒤, DB가 부여한 PK(id)만 꺼내서 반환
        return addressRepository.save(address).getId();
    }

    /**
     * 회원의 배송지 목록 조회. 해당 회원의 '활성' 상태 배송지만 응답 DTO 리스트로 변환해 돌려준다.
     */
    public List<AddressResponse> list(Long memberId) {
        // 회원 존재 여부만 확인하면 되므로, 엔티티 전체를 가져오는 findById 대신 가벼운 existsById 사용
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MEMBER_NOT_FOUND);
        }
        // 파생 쿼리: 메서드 이름이 곧 SQL → WHERE member_id = ? AND status = ? 로 변환됨 (활성 배송지만 조회)
        return addressRepository.findByMemberIdAndStatus(memberId, ActiveStatus.ACTIVE)
                .stream()
                .map(AddressResponse::from)   // 각 Address 엔티티를 응답 DTO 로 변환 (AddressResponse 의 static 팩토리 메서드 from)
                .toList();               // 변환 결과를 List 로 모아서 반환
    }
}
