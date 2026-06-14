package com.kuit.baemin.dto.response;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.menu.Menu;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 메뉴 "응답" DTO.
 * 클라이언트(앱/브라우저)에게 내려줄 메뉴 데이터만 담는 그릇이다.
 * Menu 엔티티를 그대로 노출하지 않고, 이 DTO로 필요한 값만 골라 응답한다.
 * (엔티티를 직접 응답하면 연관관계 지연로딩·민감정보 노출 등 문제가 생길 수 있어 DTO로 분리)
 * 메뉴 하나가 여러 옵션그룹(OptionGroupResponse)을 가지므로, 응답도 중첩 구조로 함께 내려준다.
 */
@Getter   // Lombok이 id/name 등의 getter를 자동 생성 (JSON 직렬화 시 필요)
@Builder  // Lombok이 빌더 패턴(.builder().id(..).name(..).build())을 자동 생성
public class MenuResponse {

    private Long id;
    private String name;
    private String description;
    private int price;
    private ActiveStatus status;                  // 메뉴 상태 enum (예: ACTIVE/판매중, INACTIVE/판매중지). JSON엔 보통 "ACTIVE" 같은 이름으로 직렬화됨
    private List<OptionGroupResponse> optionGroups;    // 이 메뉴에 속한 옵션그룹들. 엔티티의 자식 컬렉션을 응답 DTO 리스트로 변환해 담는다 (중첩 응답)

    // 엔티티(Menu) -> 응답 DTO(MenuResponse) 변환용 static 팩토리 메서드.
    // 'from'은 "다른 타입 하나를 받아 이 타입으로 만든다"는 관례적 이름.
    // 컨트롤러/서비스에서 MenuResponse.from(menu) 형태로 호출한다.
    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())                   // 엔티티의 PK를 응답 id로 복사
                .name(menu.getName())               // 엔티티의 이름을 응답 name으로 복사
                .description(menu.getDescription())
                .price(menu.getPrice())
                .status(menu.getStatus())
                // 자식 엔티티 목록(List<OptionGroup>)을 응답 DTO 목록(List<OptionGroupResponse>)으로 변환.
                // stream()으로 하나씩 꺼내 OptionGroupResponse.from()을 적용(map)하고, toList()로 다시 리스트로 모은다.
                .optionGroups(menu.getOptionGroups().stream()
                        .map(OptionGroupResponse::from)
                        .toList())
                .build();
    }
}
