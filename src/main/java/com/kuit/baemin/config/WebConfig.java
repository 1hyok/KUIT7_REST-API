package com.kuit.baemin.config;

import com.kuit.baemin.auth.LoginMemberArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 커스터마이징 설정.
 *
 * <p>핵심: {@code @EnableWebMvc} 를 '붙이지 않는다'. 붙이면 Spring Boot 의 MVC 자동구성이 꺼지므로,
 * 자동구성은 그대로 둔 채 추가 설정만 얹으려면 WebMvcConfigurer 만 구현해야 한다. (Spring Boot 레퍼런스 권장)
 * 여기선 {@code @Login LoginMember} 주입용 ArgumentResolver 를 등록한다.
 */
@Configuration                            // 여기선 @Bean이 없어 'full 모드 프록시' 기능은 안 쓰임. 역할은 하나 — 이 클래스를 빈으로 등록(=@Component 효과)해, 스프링 부트 MVC 자동구성이 발견하고 WebMvcConfigurer 콜백(addArgumentResolvers)을 호출하게 함
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    /** 커스텀 ArgumentResolver 등록 — 내장 리졸버에 '추가'되며, 빌트인 동작을 덮어쓰지 않는다 */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
