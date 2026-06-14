# KUIT 7주차 미션 — JPA로 배달앱 REST API 구현하기

## 시작 전 설정

1. 환경변수 설정 — `application.yml`에 아래 환경변수를 본인 로컬 환경에 맞게 주입

```yaml
spring:
  datasource:
    url: ${DATASOURCE_URL_LOCAL}      # jdbc:mysql://localhost:3306/{생성한 DB 이름}
    username: ${DATASOURCE_USERNAME}  # 본인 DB username
    password: ${DATASOURCE_PASSWORD}  # 본인 DB 비밀번호
    # driver-class-name 생략 — url의 jdbc:mysql:// 접두사로 Spring Boot가 자동 판별
```

2. MySQL에서 DB 생성 (테이블 이름은 자유)

```sql
CREATE DATABASE baemin DEFAULT CHARACTER SET utf8mb4;
```

---

## 완성된 API (참고용)

| Method | URI | 설명 |
|--------|-----|------|
| POST | /members | 회원 가입 |
| POST | /members/login | 로그인 |
| GET | /members/{memberId} | 회원 단건 조회 |

---

## 미션: 직접 구현할 API

6주차에 설계한 본인의 ERD를 기반으로 **10개** API를 자유롭게 설계하고 구현하세요.
API 종류와 URI는 본인 ERD에 맞게 결정하면 됩니다.
단, GET/POST/DELETE 등.. 다양하게 섞어서 구현해주세요

> 조회 API의 경우 페이징 처리를 적용하여 API 성능을 개선해보세요! (선택)

---

## PR 메시지 작성 방법

PR 본문에는 본인이 구현한 **10개 API 목록**을 아래 형식으로 작성해주세요.

### 구현한 API 목록

| Method | URI                 | 설명       |
|--------|---------------------|----------|
| POST   | /members            | 회원 가입    |
| ...    | ...                 | ...      |

#### 기타 특이사항
(페이징 적용 여부, 어려웠던 점, 코드 설명 등 자유롭게 작성)

---

## ✅ 구현한 API 목록 (미션 결과)

> `members` 3종은 제공된 참고용 API이며, 아래 **13종**이 6주차 ERD 기반으로 직접 구현한 API입니다.

| #  | Method | URI                                | 설명                              | 페이징 |
|----|--------|------------------------------------|-----------------------------------|:------:|
| 1  | POST   | /categories                        | 카테고리 생성                     |        |
| 2  | GET    | /categories                        | 카테고리 목록 조회                | ✅     |
| 3  | POST   | /restaurants                       | 가게 등록                         |        |
| 4  | GET    | /restaurants                       | 가게 목록 조회 (categoryId 필터)  | ✅     |
| 5  | GET    | /restaurants/{restaurantId}        | 가게 상세 조회 (메뉴 목록 포함)   |        |
| 6  | POST   | /restaurants/{restaurantId}/menus  | 메뉴 등록 (옵션 그룹/옵션 포함)   |        |
| 7  | GET    | /restaurants/{restaurantId}/menus  | 가게 메뉴 목록 조회               | ✅     |
| 8  | POST   | /members/{memberId}/addresses      | 배송지 등록                       |        |
| 9  | GET    | /members/{memberId}/addresses      | 회원 배송지 목록 조회             |        |
| 10 | POST   | /orders                            | 주문 생성                         |        |
| 11 | GET    | /members/{memberId}/orders         | 회원별 주문 목록 조회             | ✅     |
| 12 | PATCH  | /orders/{orderId}/status           | 주문 진행 상태 변경               |        |
| 13 | DELETE | /orders/{orderId}                  | 주문 취소                         |        |

### 기타 특이사항
- **ERD 전체 반영**: `category, restaurant, menu, option_group, menu_option, address, orders, order_item, order_item_option` 9개 도메인을 JPA 엔티티 + 연관관계(`@ManyToOne` / `@OneToMany`)로 매핑했습니다. (`user`는 제공된 참고용 `Member`로 대체)
- **페이징**: 목록 조회 4종에 Spring Data `Pageable`을 적용하고, 응답은 공통 `PageResponse`로 일관되게 반환합니다.
- **주문 로직**: 주문 생성 시 메뉴/옵션 가격을 스냅샷(`price_at_order`)으로 저장하고, 최소 주문 금액을 검증하며, `(메뉴+옵션 합계) + 배달비`로 총액을 계산합니다. `cascade`로 `order_item` / `order_item_option`을 함께 저장합니다.
- **상태/예외 처리**: 소프트 삭제용 공통 `ActiveStatus`, 주문 진행 상태 `OrderStatus`를 사용하고, 도메인별 예외와 `ErrorStatus`로 표준 에러 응답(HTTP 상태코드 반영)을 제공합니다.
- **N+1 완화**: `hibernate.default_batch_fetch_size` 설정으로 지연 로딩 시 IN 절 배치 조회를 적용했습니다.
- **문서화**: `springdoc-openapi`(Swagger UI) — 실행 후 `/swagger-ui.html` 접속.

---

## 📖 코드 읽는 순서 (구조 이해용)

> 경로 기준: `src/main/java/com/kuit/baemin/` (설정 파일만 예외). **도메인·요청 DTO·컨트롤러·서비스·레포지토리·응답 DTO(`domain/*`, `dto/request/*`, `controller/*`, `service/*`, `repository/*`, `dto/response/*`)를 계층별로 한 번에 모아 읽는다(아래 2~7번).**

### 0. 진입점
1. `BaeminApplication.java`

### 1. 설정 · 공통
1. `src/main/resources/application.yml`
2. `src/test/resources/application.yml`
3. `common/domain/ActiveStatus.java`
4. `common/domain/ActiveStatusConverter.java`
5. `common/dto/ApiResponse.java`
6. `common/dto/BaseCode.java`
7. `common/dto/SuccessStatus.java`
8. `dto/response/PageResponse.java`

### 2. 도메인 (한 번에 모아 읽기)
> `domain/*` 패키지의 엔티티·enum·컨버터·공통 부모(BaseEntity)를 한 번에. (DB 테이블 모델이라 먼저 보면 전체 구조가 잡힘)
1. `domain/BaseEntity.java`
2. `domain/member/Member.java`
3. `domain/category/Category.java`
4. `domain/Restaurant/Restaurant.java`
5. `domain/menu/Menu.java`
6. `domain/menu/OptionGroup.java`
7. `domain/menu/MenuOption.java`
8. `domain/menu/SelectionType.java`
9. `domain/menu/SelectionTypeConverter.java`
10. `domain/address/Address.java`
11. `domain/address/AddressType.java`
12. `domain/address/AddressTypeConverter.java`
13. `domain/order/Order.java`
14. `domain/order/OrderItem.java`
15. `domain/order/OrderItemOption.java`
16. `domain/order/OrderStatus.java`
17. `domain/order/OrderStatusConverter.java`

### 3. 요청 DTO (한 번에 모아 읽기)
> 검증 어노테이션(`@NotBlank`/`@NotNull`/`@Valid` 등) 패턴이 비슷해, 도메인별로 흩지 않고 한 번에 읽는다.

**Member**
1. `dto/request/SignUpRequest.java`
2. `dto/request/LoginRequest.java`

**Restaurant**
3. `dto/request/RestaurantCreateRequest.java`

**Category**
4. `dto/request/CategoryCreateRequest.java`

**Menu**
5. `dto/request/MenuCreateRequest.java`
6. `dto/request/OptionGroupRequest.java`
7. `dto/request/MenuOptionRequest.java`

**Address**
8. `dto/request/AddressCreateRequest.java`

**Order**
9. `dto/request/OrderCreateRequest.java`
10. `dto/request/OrderItemRequest.java`
11. `dto/request/OrderStatusUpdateRequest.java`

### 4. 컨트롤러 (한 번에 모아 읽기)
> 컨트롤러는 요청을 받아 Service에 위임만 하는 얇은 입구라, 한 번에 훑으면 전체 API 윤곽이 잡힌다.
1. `controller/CategoryController.java`
2. `controller/MemberController.java`
3. `controller/RestaurantController.java`
4. `controller/MenuController.java`
5. `controller/AddressController.java`
6. `controller/OrderController.java`

### 5. 서비스 (한 번에 모아 읽기)
> 서비스는 비즈니스 로직 본체라, 한 번에 보면 전체 처리 흐름이 잡힌다.
1. `service/CategoryService.java`
2. `service/MemberService.java`
3. `service/RestaurantService.java`
4. `service/MenuService.java`
5. `service/AddressService.java`
6. `service/OrderService.java`

### 6. 레포지토리 (한 번에 모아 읽기)
> 레포지토리는 인터페이스 선언만 있고 구현은 Spring Data JPA가 런타임에 자동 생성. 파생 쿼리·`@EntityGraph` 위주라 한 번에 훑기 좋다.
1. `repository/CategoryRepository.java`
2. `repository/MemberRepository.java`
3. `repository/RestaurantRepository.java`
4. `repository/MenuRepository.java`
5. `repository/MenuOptionRepository.java`
6. `repository/AddressRepository.java`
7. `repository/OrderRepository.java`

### 7. 응답 DTO (한 번에 모아 읽기)
> 엔티티 → 화면에 필요한 값만 골라 담는 응답 DTO를 한 번에. (도메인별로 묶어 정리)

**Category**
1. `dto/response/CategoryResponse.java`

**Member**
2. `dto/response/MemberResponse.java`

**Restaurant**
3. `dto/response/RestaurantResponse.java`
4. `dto/response/RestaurantDetailResponse.java`

**Menu**
5. `dto/response/MenuResponse.java`
6. `dto/response/OptionGroupResponse.java`
7. `dto/response/MenuOptionResponse.java`

**Address**
8. `dto/response/AddressResponse.java`

**Order**
9. `dto/response/OrderResponse.java`
10. `dto/response/OrderItemResponse.java`
11. `dto/response/OrderItemOptionResponse.java`

### 8. 예외 · 문서
1. `exception/errorcode/ErrorStatus.java`
2. `exception/GeneralException.java`
3. `exception/MemberException.java`
4. `exception/CategoryException.java`
5. `exception/RestaurantException.java`
6. `exception/MenuException.java`
7. `exception/AddressException.java`
8. `exception/OrderException.java`
9. `exception/handler/GlobalExceptionHandler.java`
10. `config/SwaggerConfig.java`

---

## API 문서화

구현한 API를 Postman 또는 Swagger로 문서화한 뒤 URL을 노션 미션 페이지에 제출하세요.