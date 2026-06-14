package com.kuit.baemin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 애플리케이션 시작점. main()이 실행되면 내장 톰캣이 뜨고 스프링이 빈들을 띄운다.
 * 이 클래스가 있는 패키지(com.kuit.baemin) 하위를 스캔해 @RestController/@Service/@Repository 등을 자동 등록한다.
 */
@EnableJpaAuditing  // BaseEntity의 createdAt/updatedAt 자동 기록(JPA Auditing) 켜기
@SpringBootApplication  // 아래 3개를 합친 어노테이션:
                        //  · @EnableAutoConfiguration = 클래스패스의 라이브러리(starter)를 보고 필요한 설정을 자동으로 해줌(예: web starter 있으면 내장 톰캣·MVC 자동) = "부트 자동설정"
                        //  · @ComponentScan = 이 패키지 하위에서 @Component류(@Service·@Repository·@RestController 등)를 찾아 스프링 빈으로 자동 등록 = "컴포넌트 스캔"
                        //  · @Configuration = 이 클래스 자체도 설정 클래스로 취급
public class BaeminApplication {

    public static void main(String[] args) {   // 프로그램 진입점 — 여기서 스프링 부트 앱 구동
        SpringApplication.run(BaeminApplication.class, args);
    }
}
