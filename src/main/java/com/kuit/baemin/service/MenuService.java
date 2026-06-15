package com.kuit.baemin.service;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.menu.Menu;
import com.kuit.baemin.domain.menu.MenuOption;
import com.kuit.baemin.domain.menu.OptionGroup;
import com.kuit.baemin.domain.restaurant.Restaurant;
import com.kuit.baemin.dto.request.MenuCreateRequest;
import com.kuit.baemin.dto.request.MenuOptionRequest;
import com.kuit.baemin.dto.request.OptionGroupRequest;
import com.kuit.baemin.dto.response.MenuResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.exception.RestaurantException;
import com.kuit.baemin.repository.MenuRepository;
import com.kuit.baemin.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.RESTAURANT_FORBIDDEN;
import static com.kuit.baemin.exception.errorcode.ErrorStatus.RESTAURANT_NOT_FOUND;

/**
 * 메뉴(Menu) 관련 비즈니스 로직을 담는 Service 계층.
 * Controller(요청 받기)와 Repository(DB 접근) 사이에서 "무엇을 어떻게 처리할지"를 책임진다.
 * 예: 가게 존재 확인 -> 메뉴/옵션 생성 -> 저장, 가게별 메뉴 목록 조회(페이징).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)     // 클래스의 모든 메서드를 기본 "읽기 전용" 트랜잭션으로. 조회 성능에 유리(쓰기 안 함)
public class MenuService {

    // 생성자 주입 대상. final 이라 한 번 주입되면 바뀌지 않음
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * 메뉴 등록 (옵션 그룹/옵션 함께 생성 — cascade)
     * Menu 하나에 OptionGroup 여러 개, 그 안에 MenuOption 여러 개가 매달린 구조를 한 번에 저장한다.
     */
    @Transactional
    public Long create(Long restaurantId, MenuCreateRequest req, Long actorMemberId, boolean isAdmin) {
        // 메뉴를 달 가게를 먼저 조회. 없으면 orElseThrow 로 예외를 던져 흐름 중단 (전역 예외 처리로 에러 응답 변환)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantException(RESTAURANT_NOT_FOUND));

        // 인가: 관리자(ADMIN)이거나 이 가게의 주인(점주)이면 허용하고, 둘 다 아니면 차단한다.
        //   - 관리자는 남의 가게여도 전부 허용하는 우회 권한
        //   - isOwnedBy 는 이 가게의 주인이 호출자인지 검사
        //   허용 조건이 "관리자 또는 주인"이므로, 막는 조건은 그 반대인 "관리자도 주인도 아님"이다(드모르간 법칙).
        if (!isAdmin && !restaurant.isOwnedBy(actorMemberId)) {
            throw new RestaurantException(RESTAURANT_FORBIDDEN);
        }

        // 요청 DTO 값으로 Menu 엔티티를 조립 (빌더 패턴). 아직 DB에 저장 전, 메모리 상의 객체일 뿐
        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .status(ActiveStatus.ACTIVE)   // 메뉴 초기 상태를 '활성'으로. 여러 도메인이 공유하는 공통 상태 enum
                .build();

        // 요청에 담긴 옵션 그룹들을 순회하며 엔티티로 변환해 메뉴에 붙인다 (예: "맵기 선택", "사이드 추가")
        for (OptionGroupRequest groupReq : req.getOptionGroups()) {
            OptionGroup group = OptionGroup.builder()
                    .name(groupReq.getName())
                    .required(groupReq.isRequired())              // 필수 선택 그룹인지 여부
                    .selectionType(groupReq.getSelectionType())   // 단일/다중 선택 등 선택 방식 enum
                    .build();
            // 한 그룹 안의 개별 옵션들을 그룹에 추가 (예: "순한맛", "매운맛")
            for (MenuOptionRequest optionReq : groupReq.getOptions()) {
                // addOption: 양방향 연관관계 편의 메서드. group.options 에 넣고 option.group 도 함께 세팅
                group.addOption(MenuOption.builder()
                        .name(optionReq.getName())
                        .price(optionReq.getPrice())    // 옵션 가격 (메뉴에 더해지는 금액)
                        .build());
            }
            // addOptionGroup: 메뉴-옵션그룹 양쪽을 연결하는 편의 메서드
            menu.addOptionGroup(group);
        }

        // menu 만 save 해도 cascade 설정 덕분에 매달린 OptionGroup/MenuOption 까지 함께 INSERT 된다.
        // 저장 후 DB가 부여한 PK(getId)를 반환
        return menuRepository.save(menu).getId();
    }

    /**
     * 가게의 메뉴 목록 (페이징)
     * Pageable(요청한 페이지 번호/크기/정렬)을 받아 해당 페이지만 잘라서 조회한다.
     */
    public PageResponse<MenuResponse> list(Long restaurantId, Pageable pageable) {
        // 엔티티 전체를 꺼낼 필요 없이 존재 여부만 확인할 땐 existsById (SELECT count/exists 로 가볍게 검사)
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantException(RESTAURANT_NOT_FOUND);
        }
        // findByRestaurantId: 메서드 이름으로 SQL이 만들어지는 파생 쿼리 (WHERE restaurant_id = ? + 페이징)
        // 결과 Page<Menu> 의 각 항목을 .map 으로 응답 DTO(MenuResponse)로 변환 후, PageResponse 로 감싸 반환
        return PageResponse.from(
                menuRepository.findByRestaurantId(restaurantId, pageable)
                        .map(MenuResponse::from)
        );
    }
}
