package com.kuit.baemin.dto.response;

import com.kuit.baemin.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 '응답 DTO'.
 * 엔티티(Member)를 그대로 응답하지 않고, 클라이언트에게 보여줄 필드만 골라 담아 내보냅니다.
 * (Member 엔티티에는 password, status 같은 민감/내부 필드가 있는데 여기엔 일부러 담지 않음 → 외부로 새어 나가지 않게)
 */
@Getter
@Builder
public class MemberResponse {

    private Long id;
    private String email;
    private String phone;
    private String name;
    private LocalDateTime createdAt;   // 가입 시각. Member가 BaseEntity에서 물려받은 createdAt을 그대로 내려줌

    /**
     * 엔티티 → 응답 DTO 변환 (정적 팩토리 메서드).
     * 'static' 이라 객체를 만들지 않고 MemberResponse.from(회원) 형태로 바로 호출합니다.
     * Service가 DB에서 꺼낸 Member를 이 메서드로 화면용 데이터로 바꿔 줍니다.
     */
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .name(member.getName())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
