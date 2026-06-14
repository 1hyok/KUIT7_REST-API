package com.kuit.baemin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 설정 클래스.
 *
 * <p>springdoc 라이브러리가 컨트롤러(@RestController)의 API들을 스캔해 자동으로 "API 문서"를 만들어 주는데,
 * 이 클래스는 그 문서의 제목/설명/버전 같은 머릿말(메타정보)을 지정하는 역할을 한다.
 * 서버를 실행한 뒤 브라우저로 <code>/swagger-ui.html</code> 에 들어가면, 여기서 정한 정보가 붙은
 * API 문서 화면이 나오고 거기서 각 API를 직접 호출해 볼 수 있다.</p>
 */
@Configuration                       // 이 클래스는 "설정용 클래스"임을 표시. 안에 정의한 @Bean들을 스프링이 시작할 때 읽어 등록한다
public class SwaggerConfig {

    @Bean                            // 메서드가 돌려주는 객체(OpenAPI)를 스프링 컨테이너에 등록 -> springdoc이 이 설정을 꺼내 문서에 적용
    public OpenAPI baeminOpenAPI() {
        // new OpenAPI()로 빈 문서 설정 객체를 만든 뒤, 점(.)으로 이어 붙이는 빌더 방식으로 값을 채워 그대로 반환한다
        return new OpenAPI()
                .info(new Info()     // info = 문서 상단에 보이는 기본 정보 묶음(제목/설명/버전)
                        .title("배달의민족 REST API")                                                        // Swagger UI 맨 위에 표시될 문서 제목
                        .description("KUIT 7주차 미션 — JPA로 배달앱 REST API 구현 (Swagger UI: /swagger-ui.html)")  // 제목 아래 부연 설명
                        .version("v1.0"));                                                                  // API 버전 표기
    }
}
