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
 * 예: {@code PageRes<RestaurantRes>}는 가게 목록 페이지, {@code PageRes<MenuRes>}는 메뉴 목록 페이지.</p>
 */
@Getter   // Lombok: 모든 필드의 getter를 자동 생성 (JSON 직렬화 시 값을 꺼내가는 통로)
@Builder  // Lombok: 빌더 패턴 코드 자동 생성 (아래 from()에서 .content(...).page(...) 형태로 조립)
public class PageRes<T> {

    private List<T> content;
    private int page;            // 현재 페이지 번호 (0-base)
    private int size;            // 페이지 크기
    private long totalElements;  // 전체 데이터 수
    private int totalPages;      // 전체 페이지 수
    private boolean first;       // 첫 페이지 여부
    private boolean last;        // 마지막 페이지 여부

    /**
     * Spring Data의 {@link Page} 객체를 받아 PageRes로 변환하는 정적 팩토리 메서드.
     * (서비스/컨트롤러에서 {@code PageRes.from(page)} 형태로 호출해 응답 객체를 만든다.)
     *
     * <p>{@code PageRes.<T>builder()}의 {@code <T>}는 타입을 명시해 주는 문법으로,
     * 정적 메서드라 컴파일러가 T를 자동 추론하기 어려워 직접 알려주는 것이다.</p>
     */
    public static <T> PageRes<T> from(Page<T> page) {
        return PageRes.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
