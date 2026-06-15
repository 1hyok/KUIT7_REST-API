package com.kuit.baemin.auth;

import com.kuit.baemin.domain.member.MemberRole;
import com.kuit.baemin.exception.AuthException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @Login LoginMember} 파라미터에, 검증된 토큰에서 뽑은 '현재 로그인 회원' 정보를 주입하는 ArgumentResolver.
 * 컨트롤러가 Spring Security 의 Jwt 타입을 직접 다루지 않게 해서 보안 프레임워크와 컨트롤러를 분리(디커플링)한다.
 * WebConfig.addArgumentResolvers 로 등록된다.
 */
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    /** 이 리졸버가 처리할 파라미터인지 판단: @Login 이 붙어 있고 타입이 LoginMember 인 경우만 */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Login.class)
                && parameter.getParameterType().equals(LoginMember.class);
    }

    /** 실제 주입할 값 생성: SecurityContext 의 검증된 Jwt 에서 회원 id·role 을 꺼내 LoginMember 로 만든다 */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // 리소스 서버가 인증에 성공하면 SecurityContext 의 principal 이 '검증을 마친 Jwt' 다
        // SecurityContextHolder = "현재 누가 인증됐나"를 담는 중앙 저장소(기본 전략 ThreadLocal → 현재 요청 스레드 전용, 인자로 안 넘겨도 꺼냄)
        // getContext() = 그 SecurityContext 객체를 꺼냄 → 안의 Authentication(인증 주체)을 가져온다. 필터가 토큰 검증 성공 시 채워둠
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // instanceof Jwt jwt = principal이 Jwt 타입인지 검사 + 맞으면 자동 캐스팅해 변수 jwt에 바인딩(Java 16+ 패턴 매칭). !(...)라 'Jwt 아니면' 걸러냄 → 아래에선 jwt 그대로 사용
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthException(ErrorStatus.UNAUTHORIZED);   // 보호 API라 정상 흐름에선 도달하지 않음(방어 코드)
        }
        Long id = Long.valueOf(jwt.getSubject());                              // sub(문자열) → Long으로 '파싱(변환)'. 캐스팅 아님 — Long 클래스에 JDK가 만들어둔 static 메서드(숫자 아니면 NumberFormatException)
        String roleClaim = jwt.getClaimAsString(JwtTokenProvider.ROLE_CLAIM);  // role 클레임 → 권한 (access 토큰엔 항상 존재)
        MemberRole role = (roleClaim != null) ? MemberRole.valueOf(roleClaim) : MemberRole.CONSUMER;   // valueOf=이름이 일치하는 enum 상수 반환("OWNER"→MemberRole.OWNER). enum이면 컴파일러가 자동 생성(없는 이름이면 IllegalArgumentException)
        return new LoginMember(id, role);
    }
}
