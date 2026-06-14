package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.menu.MenuOption;
import lombok.Builder;
import lombok.Getter;

/**
 * 메뉴 옵션(MenuOption) 한 건을 클라이언트에게 내려줄 때 쓰는 응답 DTO.
 * <p>
 * DTO(Data Transfer Object)는 계층 간 데이터 전달 전용 객체다.
 * 엔티티(MenuOption)를 API 응답으로 그대로 노출하면 DB 구조나 불필요한 연관관계까지 새어 나가므로,
 * 화면에 꼭 필요한 값(id/이름/추가금액)만 골라 담은 이 DTO로 변환해서 응답한다.
 */
@Getter                                   // Lombok: 각 필드의 getter(getId/getName/getExtraPrice)를 컴파일 시 자동 생성. JSON 직렬화 시 이 getter들이 사용됨
@Builder                                  // Lombok: 빌더 패턴 생성. 아래 from()에서 .id().name()... 처럼 필드를 골라 객체를 조립할 수 있게 함
public class MenuOptionResponse {

    private Long id;
    private String name;
    private int extraPrice;               // 옵션 선택 시 추가되는 금액

    /**
     * 엔티티(MenuOption) -> 응답 DTO(MenuOptionResponse) 변환용 정적 팩토리 메서드.
     * 객체 생성 로직을 이 한 곳에 모아 두면, 컨트롤러/서비스에서 MenuOptionResponse.from(option) 한 줄로 변환할 수 있다.
     */
    public static MenuOptionResponse from(MenuOption option) {
        return MenuOptionResponse.builder()    // @Builder가 만들어 준 빌더로 필요한 필드만 채워서 객체 생성
                .id(option.getId())
                .name(option.getName())
                .extraPrice(option.getExtraPrice())
                .build();
    }
}
