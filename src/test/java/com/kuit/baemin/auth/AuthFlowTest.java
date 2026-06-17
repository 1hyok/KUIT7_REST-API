package com.kuit.baemin.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.baemin.common.domain.ActiveStatus;
import com.kuit.baemin.domain.category.Category;
import com.kuit.baemin.domain.member.Member;
import com.kuit.baemin.domain.member.MemberRole;
import com.kuit.baemin.domain.restaurant.Restaurant;
import com.kuit.baemin.repository.CategoryRepository;
import com.kuit.baemin.repository.MemberRepository;
import com.kuit.baemin.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 인증/인가 통합 테스트 — 실제 Spring 컨텍스트(Spring Security 필터체인 포함)를 띄워 HTTP 동작을 검증한다.
 * Redis는 @MockitoBean으로 대체(실제 Redis 불필요), DB는 테스트용 H2(create-drop)를 쓴다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired MemberRepository memberRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired RestaurantRepository restaurantRepository;

    // 로그인은 refresh를 Redis에 저장하는데, 테스트에선 Redis 없이 돌도록 저장소를 가짜(no-op)로 대체
    @MockitoBean RefreshTokenRepository refreshTokenRepository;

    // 이메일·전화번호 모두 unique 제약이라 가입마다 다른 값을 넣는다
    private static final String SIGNUP_JSON =
            "{\"email\":\"%s\",\"password\":\"password123\",\"name\":\"테스트\",\"phone\":\"%s\"}";
    private static final String LOGIN_JSON =
            "{\"email\":\"%s\",\"password\":\"password123\"}";

    @Test
    void 토큰_없이_보호API_접근하면_401() throws Exception {
        mockMvc.perform(get("/members/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 가입_로그인하면_토큰이_발급되고_그_토큰으로_보호API에_접근된다() throws Exception {
        String email = "flow@test.com";
        long memberId = signup(email, "010-1111-1111");
        String accessToken = login(email);

        mockMvc.perform(get("/members/" + memberId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void refresh토큰을_access처럼_쓰면_401() throws Exception {
        // type=refresh 토큰 (서명·만료는 유효). 리소스 서버 디코더가 type=access가 아니라고 거부해야 함
        String refreshToken = jwtTokenProvider.createRefreshToken(999L);
        mockMvc.perform(get("/members/1").header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 일반회원은_가게등록이_403이고_점주토큰은_역할게이트를_통과한다() throws Exception {
        String email = "consumer@test.com";
        long memberId = signup(email, "010-2222-2222");
        String consumerToken = login(email);   // 가입 기본 role = CONSUMER

        // CONSUMER → POST /categories → 403 (역할 부족, 컨트롤러 도달 전 차단)
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + consumerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // OWNER 토큰 → 역할 게이트 통과 → 403이 아님 (본문 검증으로 400이 나도 '403만 아니면' 인가 통과를 의미)
        String ownerToken = jwtTokenProvider.createAccessToken(memberId, MemberRole.OWNER);
        MvcResult result = mockMvc.perform(post("/categories").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    void 전화번호가_중복이면_409() throws Exception {
        signup("phone-a@test.com", "010-3333-3333");
        // 같은 전화번호 + 다른 이메일 → 깔끔한 409 (이전엔 DB 유니크 위반으로 500이었음)
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNUP_JSON.formatted("phone-b@test.com", "010-3333-3333")))
                .andExpect(status().isConflict());
    }

    @Test
    void 메뉴등록은_그_가게의_점주만_가능하다() throws Exception {
        // 점주 2명 + 카테고리 + 가게(주인=A)를 DB에 직접 준비
        Member ownerA = memberRepository.save(owner("ownerA@test.com", "010-4444-4444"));
        Member ownerB = memberRepository.save(owner("ownerB@test.com", "010-5555-5555"));
        Category category = categoryRepository.save(Category.create("치킨-테스트"));
        Restaurant r = restaurantRepository.save(Restaurant.builder()
                .category(category).owner(ownerA)
                .name("가게A").phone("02-111-2222").address("서울시 어딘가")
                .minOrderPrice(10000).deliveryFee(3000).status(ActiveStatus.ACTIVE).build());

        String menuBody = "{\"name\":\"후라이드\",\"price\":18000}";   // optionGroups 생략 가능(빈 리스트)
        String tokenA = jwtTokenProvider.createAccessToken(ownerA.getId(), MemberRole.OWNER);
        String tokenB = jwtTokenProvider.createAccessToken(ownerB.getId(), MemberRole.OWNER);

        // 주인 A → 통과(403 아님)
        MvcResult okByOwner = mockMvc.perform(post("/restaurants/" + r.getId() + "/menus")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON).content(menuBody))
                .andReturn();
        assertThat(okByOwner.getResponse().getStatus()).isNotEqualTo(403);

        // 다른 점주 B → 403 (역할은 OWNER라 게이트는 통과하지만, 본인 가게가 아니라 서비스에서 막힘)
        mockMvc.perform(post("/restaurants/" + r.getId() + "/menus")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON).content(menuBody))
                .andExpect(status().isForbidden());
    }

    // ── 헬퍼 ──
    private Member owner(String email, String phone) {
        return Member.builder().email(email).password("x").name("점주").phone(phone)
                .status(ActiveStatus.ACTIVE).role(MemberRole.OWNER).build();
    }

    private long signup(String email, String phone) throws Exception {
        MvcResult res = mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON).content(SIGNUP_JSON.formatted(email, phone)))
                .andExpect(status().isCreated())   // 회원 가입은 새 리소스 생성 → 201 Created
                .andReturn();
        return result(res).asLong();
    }

    private String login(String email) throws Exception {
        MvcResult res = mockMvc.perform(post("/members/login")
                        .contentType(MediaType.APPLICATION_JSON).content(LOGIN_JSON.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return result(res).get("accessToken").asText();
    }

    /** ApiResponse 공통 봉투에서 result 노드만 꺼낸다 */
    private JsonNode result(MvcResult res) throws Exception {
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("result");
    }
}
