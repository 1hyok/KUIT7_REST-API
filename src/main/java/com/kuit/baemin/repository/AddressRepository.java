package com.kuit.baemin.repository;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // 회원의 활성 배송지 목록
    // findBy + Member_Id + And + Status → WHERE user_id = ? AND status = ? 인 SELECT 가 생성됨.
    //   (Member 는 연관 엔티티라 그 안의 id(=address.user_id FK)로 거름 / status 로 INACTIVE(숨김) 제외)
    // 반환이 List = 한 회원이 여러 배송지를 가질 수 있어 여러 건을 목록으로 받음.
    List<Address> findByMemberIdAndStatus(Long memberId, ActiveStatus status);
}
