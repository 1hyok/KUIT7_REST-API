package com.kuit.baemin.config;

import com.kuit.baemin.auth.JwtAccessDeniedHandler;
import com.kuit.baemin.auth.JwtAuthenticationEntryPoint;
import com.kuit.baemin.auth.JwtTokenProvider;
import com.kuit.baemin.auth.TokenType;
import com.kuit.baemin.domain.member.MemberRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 설정. 이 프로젝트 인증/인가의 '중심 스위치보드'.
 *
 * <p>핵심 아이디어: 세션을 쓰지 않는(stateless) JWT 방식.
 * 매 요청의 {@code Authorization: Bearer <accessToken>} 헤더를 Spring Security가 가로채
 * 우리 {@link #jwtDecoder()}로 검증 → 통과하면 '인증된 사용자'로 처리하고, 아니면 401을 돌려준다.
 * (8주차 미션이 Interceptor로 손수 하던 일을 프레임워크가 대신 해 주는, 권장되는 정석 방식)
 */
@Configuration
// 이 클래스 = 빈 정의 소스(설정 클래스). 안의 @Bean 메서드가 반환하는 객체를 스프링 컨테이너에 빈으로 등록. 기본 'full 모드'(CGLIB 프록시)라 @Bean끼리 호출해도 같은 싱글톤 반환
@EnableWebSecurity                                   // Spring Security 웹 보안 활성화 (SecurityFilterChain 빈을 읽어 필터 구성)
public class SecurityConfig {

    // 인가 규칙에서 쓰는 역할 이름. MemberRole enum에서 끌어와 '단일 출처'로 — enum 이름이 바뀌면 자동 반영되고, 리터럴 중복/오타를 막는다(SonarLint S1192도 해소)
    private static final String OWNER = MemberRole.OWNER.name();
    private static final String ADMIN = MemberRole.ADMIN.name();

    private final SecretKey secretKey;               // HS256 서명/검증에 쓰는 대칭키(같은 키로 서명하고 검증)
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;   // 인증 실패(401) 응답 처리기
    private final JwtAccessDeniedHandler accessDeniedHandler;             // 인가 실패(403) 응답 처리기

    public SecurityConfig(JwtProperties jwtProperties,   // secret을 @Value 대신 JwtProperties(@ConfigurationProperties)에서 주입
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler) {
        // SecretKeySpec(키바이트, 알고리즘이름): 이미 가진 raw 바이트를 즉석에서 SecretKey(대칭키)로 감싸는 클래스 (KeyGenerator/KeyFactory 불필요). 여기선 secret을 HMAC-SHA256용 키로
        // getBytes(UTF_8): 문자열을 UTF-8 규칙으로 byte[]로 '인코딩(변환)'한다 — 읽는 게 아니라 String→bytes 변환. SecretKeySpec은 HMAC 키를 raw 바이트로 받으므로 필요
        this.secretKey = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * 보안 필터 체인 — "어떤 URL은 공개, 어떤 URL은 인증 필요"와 토큰 검증 방식을 정의한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {   // @Bean: 이 메서드가 SecurityFilterChain '생성 레시피' — 반환값이 빈으로 등록됨. 파라미터 HttpSecurity는 Spring이 빈으로 자동 주입
        http
                // JWT는 서버에 상태를 저장하지 않으므로(쿠키 세션 X) CSRF 토큰이 필요 없음 → 끔
                .csrf(csrf -> csrf.disable())   // csrf(...) 인자는 Customizer 람다 — csrf 설정 객체를 받아 .disable() 호출 = "CSRF 보호 끄기" (Spring Security 6 람다 DSL)
                // 세션을 아예 만들지 않음(STATELESS). 인증 상태는 매 요청의 토큰으로만 판단
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ── 공개(인증 불필요) ──
                        .requestMatchers(HttpMethod.POST, "/members", "/members/login").permitAll()  // 회원가입·로그인
                        .requestMatchers(HttpMethod.POST, "/auth/reissue").permitAll()                // 토큰 재발급(refresh로). 메서드 고정
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()  // API 문서
                        // 카탈로그 둘러보기는 공개(GET만). '*'는 한 세그먼트만 매칭하므로, 나중에 /restaurants/{id}/주문 같은 보호 GET이 생겨도 자동으로 열리지 않음
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/restaurants", "/restaurants/*").permitAll()             // 가게 목록·상세
                        .requestMatchers(HttpMethod.GET, "/restaurants/*/menus", "/restaurants/*/menus/**").permitAll()  // 메뉴 카탈로그
                        // ── 점주/관리자 전용 쓰기 (역할 기반 인가). hasAnyRole("OWNER")=권한 ROLE_OWNER 보유 확인 ──
                        .requestMatchers(HttpMethod.POST, "/restaurants", "/categories").hasAnyRole(OWNER, ADMIN)  // 가게·카테고리 등록
                        .requestMatchers(HttpMethod.POST, "/restaurants/*/menus").hasAnyRole(OWNER, ADMIN)          // 메뉴 등록
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/status").hasAnyRole(OWNER, ADMIN)             // 주문 상태 변경
                        // ── 그 외 모든 요청은 인증만 되면 OK (주문 생성·본인 주문/배송지 조회 등) ──
                        .anyRequest().authenticated())
                // 리소스 서버: 들어온 Bearer 토큰을 jwtDecoder 빈으로 검증(서명+만료). 통과 시 인증 처리
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)   // 토큰 없음/유효X → 401(우리 JSON)
                        .accessDeniedHandler(accessDeniedHandler)             // 권한 부족 → 403(우리 JSON)
                        // 토큰의 role 클레임 → ROLE_ 권한으로 변환(아래 컨버터). 디코더는 jwtDecoder() 빈이 자동 사용됨
                        // 이름 겹침 주의(자기참조 아님): jwt=설정객체(파라미터) / jwt.jwtAuthenticationConverter(X)=그 객체의 '세터' / 인자 jwtAuthenticationConverter()=우리 메서드(this) 호출로 변환기 생성(142행)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // 위 oauth2 설정 밖에서 발생하는 인증/인가 실패도 같은 핸들러로 통일
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    /**
     * 토큰 발급기: 비밀키로 JWT에 HS256 서명. 로그인/재발급 때 JwtTokenProvider가 사용
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        // ImmutableSecret: 대칭 비밀키 하나를 JWK 소스로 감싸 NimbusJwtEncoder에 넘기는 어댑터
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /**
     * HS256 대칭키를 빈으로 노출 (JwtTokenProvider가 refresh 검증용 디코더를 만들 때 주입받음)
     */
    @Bean
    public SecretKey jwtSecretKey() {
        return secretKey;
    }

    /**
     * 리소스 서버용 토큰 검증기: Bearer 토큰의 서명·만료에 더해 type=access 인지까지 검사한다.
     * → refresh 토큰(type=refresh)을 보호 API 접근에 쓰는 것을 차단 (refresh는 재발급에서만 사용).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)   // 발급(HS256)과 검증 알고리즘을 일치
                .build();
        // 기본 검증(서명+만료)에 'type=access' 검증을 추가로 위임 연결
        // OAuth2TokenValidator<Jwt> = Jwt 토큰 하나를 검증해 결과(성공/실패)를 돌려주는 함수형 인터페이스(메서드 validate(Jwt) 1개). 아래 람다가 그 validate 구현
        OAuth2TokenValidator<Jwt> accessTypeValidator = jwt ->
                TokenType.from(jwt.getClaimAsString(JwtTokenProvider.TOKEN_TYPE_CLAIM)) == TokenType.ACCESS
                        ? OAuth2TokenValidatorResult.success()   // OAuth2TokenValidatorResult=검증 결과 묶음(통과/실패 + 실패 시 에러목록). success()=에러 없는 통과
                        : OAuth2TokenValidatorResult.failure(    // failure(에러...)=실패 결과 + 사유(OAuth2Error) 담음
                        new OAuth2Error("invalid_token", "access 토큰이 아닙니다.", null));   // OAuth2Error=OAuth2 규격 에러 1건(코드+설명+URI). "invalid_token"=표준 에러코드(RFC 6749)
        // setJwtValidator=디코더의 클레임 검증기를 '교체'(추가 아님→기본검증 다시 넣어야 함). Delegating=여러 검증기를 묶어 모두 통과해야 OK. createDefault()=표준 기본검증(만료 exp 등)
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), accessTypeValidator));   // → 기본 검증 + 우리 type=access 검증 둘 다 적용
        return decoder;
    }

    /**
     * JWT의 role 클레임(예: "OWNER")을 Spring Security 권한 ROLE_OWNER로 매핑하는 변환기 → hasRole/hasAnyRole 인가에 사용
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthorityPrefix("ROLE_");                            // 변환된 권한 앞에 붙일 접두사. hasRole("OWNER")=권한 ROLE_OWNER 확인이라 ROLE_ (기본값 "SCOPE_"는 OAuth2 scope용 → 역할용으로 교체)
        authorities.setAuthoritiesClaimName(JwtTokenProvider.ROLE_CLAIM);   // JWT의 어느 클레임에서 권한을 읽을지. "role" 클레임 사용 (기본은 scope/scp → 우리 커스텀 role로 교체)
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);          // 위 권한 변환기를 인증 변환기에 장착 → 인증 시 role 클레임이 ROLE_ 권한으로 변환돼 Authentication에 실림
        return converter;
    }

    /**
     * 비밀번호 해시기(BCrypt). 가입 시 해시로 저장하고, 로그인 시 matches로 비교 (평문 저장 금지)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // BCrypt: ①단방향 해시(복원 불가) ②비밀번호마다 랜덤 salt를 해시에 내장(같은 비번도 매번 다른 결과→레인보우테이블 무력) ③의도적으로 느림(work factor 기본 10라운드→무차별대입 비싸게)
    }
}
