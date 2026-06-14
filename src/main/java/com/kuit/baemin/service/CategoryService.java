package com.kuit.baemin.service;

import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.category.Category;
import com.kuit.baemin.dto.request.CategoryCreateRequest;
import com.kuit.baemin.dto.response.CategoryResponse;
import com.kuit.baemin.dto.response.PageResponse;
import com.kuit.baemin.exception.CategoryException;
import com.kuit.baemin.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kuit.baemin.exception.errorcode.ErrorStatus.DUPLICATE_CATEGORY_NAME;

/**
 * 카테고리(음식 분류) 관련 비즈니스 로직을 담는 Service 계층.
 * Controller(요청 받기)와 Repository(DB 접근) 사이에서 "실제 처리 규칙"을 책임진다.
 * 예: 중복 검사, 엔티티 생성, 조회 결과를 응답 DTO로 변환 등.
 */
@Service                            // 이 클래스를 스프링이 관리하는 Service 빈으로 등록 (다른 곳에서 주입받아 사용 가능)
@RequiredArgsConstructor            // final 필드만 받는 생성자를 자동 생성 -> 스프링이 그 생성자로 의존성 주입(DI)
@Transactional(readOnly = true)     // 클래스 전체 기본값: 읽기 전용 트랜잭션. 조회만 하는 메서드의 성능에 유리
public class CategoryService {

    private final CategoryRepository categoryRepository;    // DB 접근 담당. final + 위 어노테이션으로 생성자 주입됨

    /**
     * 카테고리 생성.
     * 같은 이름이 이미 있으면 예외를 던지고, 없으면 새로 저장한 뒤 생성된 PK(id)를 반환한다.
     */
    @Transactional      // 클래스 기본값(readOnly)을 덮어써서 쓰기 가능한 트랜잭션으로 실행 (save로 DB를 변경하므로 필요)
    public Long create(CategoryCreateRequest req) {
        // existsByName: 메서드 이름으로 SQL이 만들어지는 파생 쿼리. name 컬럼에 같은 값이 있는지 확인 (SELECT ... EXISTS)
        if (categoryRepository.existsByName(req.getName())) {
            throw new CategoryException(DUPLICATE_CATEGORY_NAME);    // 중복이면 예외 -> 전역 예외 처리기가 에러 응답으로 변환
        }
        Category category = Category.create(req.getName());     // 정적 팩토리 메서드로 엔티티 생성 (new 대신 의미 있는 생성 방식)
        return categoryRepository.save(category).getId();       // 저장 후, DB가 auto_increment로 부여한 PK를 꺼내 반환
    }

    /**
     * 활성(ACTIVE) 상태인 카테고리 목록을 페이지 단위로 조회한다.
     * Pageable(몇 페이지를 몇 개씩, 정렬은 어떻게)은 Controller에서 넘어온다.
     */
    public PageResponse<CategoryResponse> list(Pageable pageable) {
        return PageResponse.from(                                                 // Page<CategoryResponse> -> 응답용 PageResponse로 한 번 더 감쌈
                categoryRepository.findByStatus(ActiveStatus.ACTIVE, pageable)   // status = ACTIVE 인 행만 페이징 조회 -> Page<Category>
                        .map(CategoryResponse::from)                                  // Page의 각 엔티티를 응답 DTO(CategoryResponse)로 변환
        );
    }
}
