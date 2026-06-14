package com.kuit.baemin.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 공통 페이징 응답(Response) DTO.
 *
 * <p>목록 조회 API에서 "한 페이지 분량의 데이터 + 페이지 정보"를 클라이언트에게 내려줄 때 쓰는 그릇이다.
 * Spring Data JPA가 페이징 결과로 돌려주는 {@link Page} 객체를 그대로 JSON으로 응답하면
 * 내부 구조가 그대로 노출되고, 직렬화(객체 -> JSON 변환) 경고가 발생할 수 있다.
 * 그래서 우리가 직접 필요한 값만 추려 담는 안정적인 응답 포맷을 따로 둔 것.</p>
 *
 * <p>{@code <T>}는 제네릭 타입 파라미터로, "어떤 종류의 데이터 목록이든" 담을 수 있게 한다.
 * 예: {@code PageResponse<RestaurantResponse>}는 가게 목록 페이지, {@code PageResponse<MenuResponse>}는 메뉴 목록 페이지.</p>
 */
@Getter   // Lombok: 모든 필드의 getter를 자동 생성 (JSON 직렬화 시 값을 꺼내가는 통로)
@Builder  // Lombok: 빌더 + "숨은 all-args 생성자(package-private)"를 자동 생성. 그래서 손으로 쓴 생성자는 없어도 생성자 자체는 존재하며, 빌더가 결국 그 생성자를 호출해 객체를 만든다. from()은 그 위에 얹은 편의 진입점일 뿐
public class PageResponse<T> {

    private List<T> content;
    private int page;            // 현재 페이지 번호 (0-base)
    private int size;            // 페이지 크기
    private long totalElements;  // 전체 데이터 수
    private int totalPages;      // 전체 페이지 수
    private boolean first;       // 첫 페이지 여부
    private boolean last;        // 마지막 페이지 여부

    /**
     * Spring Data의 {@link Page} 객체를 받아 PageResponse로 변환하는 정적 팩토리 메서드.
     * (서비스/컨트롤러에서 {@code PageResponse.from(page)} 형태로 호출해 응답 객체를 만든다.)
     *
     * <p>{@code PageResponse.<T>builder()}의 {@code <T>}는 타입을 명시해 주는 문법으로,
     * 정적 메서드라 컴파일러가 T를 자동 추론하기 어려워 직접 알려주는 것이다.</p>
     *
     * <p>메서드 이름 {@code from}은 빌더 전용 이름이 아니다. "다른 타입 하나(Page)를 받아
     * 이 타입(PageResponse)으로 변환해 만든다"는 뜻의 정적 팩토리 관례 이름일 뿐이다
     * (JDK의 {@code Date.from(Instant)}, {@code LocalDate.from(...)} 와 같은 결).
     * 여러 값을 모아 만들 땐 보통 {@code of(...)}를 쓴다. 빌더로 만들든 생성자로 만들든
     * 이름은 자유이고, 여기선 "변환"이라 from을 골랐을 뿐 — 빌더는 그 안에서 쓰는 별개의 도구다.</p>
     */
    public static <T> PageResponse<T> from(Page<T> page) {   // page: JPA가 페이징 조회로 돌려준 결과(이번 페이지 데이터 + 페이지 메타정보)
        return PageResponse.<T>builder()                     // 빌더 시작. <T>는 "이 PageResponse가 담을 데이터 타입"을 명시 (정적 메서드라 추론이 안 돼 직접 알려줌)
                .content(page.getContent())             // 이번 페이지의 실제 데이터 목록(List<T>)을 꺼내 그대로 옮김
                .page(page.getNumber())                 // 현재 페이지 번호 (0부터 시작 — 첫 페이지가 0)
                .size(page.getSize())                   // 한 페이지에 담기로 한 최대 개수
                .totalElements(page.getTotalElements()) // 조건에 맞는 전체 데이터 개수 (모든 페이지를 합친 총합)
                .totalPages(page.getTotalPages())       // 전체 페이지 수 (= 올림(totalElements / size))
                .first(page.isFirst())                  // 지금이 첫 페이지인가? (true/false)
                .last(page.isLast())                    // 지금이 마지막 페이지인가? (true/false)
                .build();                               // 위에서 채운 값들로 PageResponse 객체를 완성해 반환
    }
}
