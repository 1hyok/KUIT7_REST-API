package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.address.Address;
import com.kuit.baemin.domain.address.AddressType;
import lombok.Builder;
import lombok.Getter;

/**
 * 주소(Address) 응답 DTO.
 * 서버가 클라이언트에게 "주소 정보"를 돌려줄 때 사용하는 출력 전용 객체.
 * Address 엔티티를 그대로 노출하지 않고, 클라이언트에게 보여줄 필드만 골라 담는다.
 * (엔티티에는 연관관계·내부 상태 등 외부에 노출하면 안 되는 정보가 섞일 수 있어 DTO로 분리)
 */
@Getter                 // Lombok: 모든 필드의 getter를 자동 생성 (JSON 직렬화 시 값을 꺼내기 위함)
@Builder                // Lombok: 빌더 패턴 자동 생성 -> AddressResponse.builder().id(..)... .build() 형태로 생성 가능
public class AddressResponse {

    private Long id;
    private AddressType type;            // 주소 유형(집/회사 등) enum. JSON에는 enum 이름 문자열로 직렬화됨
    private String alias;                // 사용자가 붙인 별칭 (예: "우리집")
    private String roadAddress;          // 도로명 주소
    private String detailAddress;        // 상세 주소 (동·호수 등)

    /**
     * Address 엔티티 -> AddressResponse 변환용 정적 팩토리 메서드.
     * "엔티티 1개를 응답 DTO 1개로 만든다"는 변환 책임을 DTO 안에 모아두는 패턴이다.
     * 서비스/컨트롤러에서는 AddressResponse.from(address) 한 줄로 변환할 수 있다.
     */
    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .type(address.getType())
                .alias(address.getAlias())
                .roadAddress(address.getRoadAddress())
                .detailAddress(address.getDetailAddress())
                .build();
    }
}
