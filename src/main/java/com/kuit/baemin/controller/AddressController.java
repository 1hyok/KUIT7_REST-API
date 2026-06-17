package com.kuit.baemin.controller;

import com.kuit.baemin.auth.Login;
import com.kuit.baemin.auth.LoginMember;
import com.kuit.baemin.common.dto.ApiResponse;
import com.kuit.baemin.dto.request.AddressCreateRequest;
import com.kuit.baemin.dto.response.AddressResponse;
import com.kuit.baemin.exception.AuthException;
import com.kuit.baemin.exception.errorcode.ErrorStatus;
import com.kuit.baemin.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * 회원 배송지(Address) API를 담당하는 컨트롤러 계층.
 * 컨트롤러는 "HTTP 요청을 받아 → Service에 일을 시키고 → 결과를 HTTP 응답으로 돌려주는" 입구 역할만 한다.
 * (실제 DB 처리/검증 같은 비즈니스 로직은 AddressService에 위임한다.)
 *
 * 이 컨트롤러의 URL은 회원에 종속된 자원(중첩 리소스)이라 항상 특정 회원 밑에 있다.
 * 예) POST /members/1/addresses → 1번 회원의 배송지 등록, GET /members/1/addresses → 1번 회원의 배송지 목록
 */
@Tag(name = "Address", description = "회원 배송지 API")          // Swagger 문서에서 이 API들을 묶는 그룹 이름/설명
@RestController
@RequestMapping("/members/{memberId}/addresses")               // 이 클래스의 모든 메서드 공통 URL prefix. {memberId}는 경로 변수(자리표시자)
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;               // 실제 작업을 위임할 서비스. 생성자 주입으로 채워짐

    @Operation(summary = "배송지 등록 (본인만)")                 // Swagger 문서에 표시될 이 API 한 줄 설명
    @PostMapping                                               // HTTP POST + 클래스의 prefix URL 매핑 (POST /members/{memberId}/addresses)
    public ResponseEntity<ApiResponse<Long>> create(@PathVariable Long memberId,
                                                    @Login LoginMember member,   // 검증된 토큰에서 꺼낸 현재 로그인 회원
                                                    @Valid @RequestBody AddressCreateRequest req) {  // 요청 JSON 본문 → DTO로 변환(@RequestBody), @Valid로 DTO의 검증 규칙 검사
        checkSelf(memberId, member);   // 인가: 남의 회원 밑에 배송지를 만들지 못하게 막음
        Long id = addressService.create(memberId, req);   // 새로 만든 배송지(Address)의 기본키(PK). DB가 INSERT 때 자동 부여. 회원 id 아님. 바로 아래 Location 만들 때만 쓰는 지역변수라 이름 'id'로 충분

        // 201 응답의 Location 헤더에 넣을 '새로 만든 배송지의 주소'를 조립한다. 단계별:
        //   fromCurrentRequest() : 지금 들어온 요청 URL(예: /members/3/addresses)에서 출발
        //   .path("/{id}")       : 그 뒤에 /{id} 한 칸 더 붙임 → /members/3/addresses/{id} (아직 {id}는 빈칸)
        //   .buildAndExpand(id)  : 빈칸 {id} 에 실제 PK(예: 7) 끼워넣음 → /members/3/addresses/7
        //   .toUri()             : 완성된 문자열을 URI 객체로 변환
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        // created(location) : 상태코드 201 Created + Location 헤더(위 location)를 세팅한 응답 빌더
        // .body(...)         : 그 응답의 본문에 공통 봉투 ApiResponse(결과=생성된 PK)를 담음
        // → 최종: HTTP 201 + Location 헤더 + JSON 본문 {isSuccess,code,message,result:id} 를 반환
        return ResponseEntity.created(location).body(ApiResponse.of(id));
    }

    @Operation(summary = "회원 배송지 목록 조회 (본인만)")
    @GetMapping                                               // HTTP GET 매핑 (GET /members/{memberId}/addresses)
    public ApiResponse<List<AddressResponse>> list(@PathVariable Long memberId,
                                                   @Login LoginMember member) {
        checkSelf(memberId, member);   // 인가: 남의 배송지 목록을 못 보게 막음
        // 응답용 DTO(AddressResponse) 리스트를 공통 응답 형식(ApiResponse)에 담아 반환
        return ApiResponse.of(addressService.list(memberId));
    }

    /** 인가 헬퍼: URL의 {memberId}가 토큰 속 로그인 회원과 다르면 '본인 것이 아님' → 403 */
    private void checkSelf(Long memberId, LoginMember member) {
        if (!memberId.equals(member.id())) {
            throw new AuthException(ErrorStatus.FORBIDDEN);
        }
    }
}
