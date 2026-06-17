package com.kuit.baemin.auth;

import java.lang.annotation.*;

/**
 * 컨트롤러 파라미터에 붙이는 표시. 이게 붙은 {@link LoginMember} 파라미터에는
 * LoginMemberArgumentResolver 가 '현재 로그인한 회원 정보'를 자동 주입한다.
 * (컨트롤러가 토큰을 직접 파싱하지 않게 해 주는 표지)
 *
 * 주의: @Login 자체는 아무 값도 뽑지 않는 '빈 표지(marker)'일 뿐이다.
 * 실제로 토큰(검증된 Jwt)에서 id·role 을 꺼내 LoginMember 를 만드는 일은 LoginMemberArgumentResolver 가 한다.
 * @Login + 타입(LoginMember) 조합은 그 리졸버에게 "이 파라미터는 내 담당이야"라고 알려주는 '조건' 역할.
 */
@Target(ElementType.PARAMETER)        // 메서드 '파라미터'에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME)   // 런타임에 리졸버가 읽어야 하므로 RUNTIME 유지
@Documented                           // 이 애너테이션을 Javadoc 문서에 포함시키라는 표시 (문서화 용도일 뿐, 동작엔 영향 없음)
public @interface Login {             // @interface = '애너테이션 타입'을 정의하는 키워드. 이 선언 자체가 @Login 을 만든다(일반 interface 아님)
}
