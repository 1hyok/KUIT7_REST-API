package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.category.Category;
import lombok.Builder;
import lombok.Getter;

/**
 * 카테고리 "응답" DTO.
 * 클라이언트(앱/브라우저)에게 내려줄 데이터만 담는 그릇이다.
 * Category 엔티티를 그대로 노출하지 않고, 이 DTO로 필요한 값(id, name)만 골라 응답한다.
 * (엔티티를 직접 응답하면 연관관계 지연로딩·민감정보 노출 등 문제가 생길 수 있어 DTO로 분리)
 */
@Getter   // Lombok이 id/name의 getId(), getName() 같은 getter를 자동 생성 (JSON 직렬화 시 필요)
@Builder  // Lombok이 빌더 패턴(.builder().id(..).name(..).build())을 자동 생성
public class CategoryResponse {

    private Long id;
    private String name;

    // 엔티티(Category) -> 응답 DTO(CategoryResponse) 변환용 static 팩토리 메서드.
    // 'from'은 "다른 타입 하나를 받아 이 타입으로 만든다"는 관례적 이름.
    // 컨트롤러/서비스에서 CategoryResponse.from(category) 형태로 호출한다.
    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())     // 엔티티의 PK를 응답 id로 복사
                .name(category.getName()) // 엔티티의 이름을 응답 name으로 복사
                .build();
    }
}
