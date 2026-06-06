# KUIT 7주차 미션 — JPA로 배달앱 REST API 구현하기

## 시작 전 설정

1. 환경변수 설정 — `application.yml`에 아래 환경변수를 본인 로컬 환경에 맞게 주입

```yaml
spring:
  datasource:
    url: ${DATASOURCE_URL_LOCAL}      # jdbc:mysql://localhost:3306/{생성한 DB 이름}
    username: ${DATASOURCE_USERNAME}  # 본인 DB username
    password: ${DATASOURCE_PASSWORD}  # 본인 DB 비밀번호
    driver-class-name: com.mysql.cj.jdbc.Driver
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
- **페이징**: 목록 조회 4종에 Spring Data `Pageable`을 적용하고, 응답은 공통 `PageRes`로 일관되게 반환합니다.
- **주문 로직**: 주문 생성 시 메뉴/옵션 가격을 스냅샷(`price_at_order`)으로 저장하고, 최소 주문 금액을 검증하며, `(메뉴+옵션 합계) + 배달비`로 총액을 계산합니다. `cascade`로 `order_item` / `order_item_option`을 함께 저장합니다.
- **상태/예외 처리**: 소프트 삭제용 공통 `ActiveStatus`, 주문 진행 상태 `OrderStatus`를 사용하고, 도메인별 예외와 `ErrorStatus`로 표준 에러 응답(HTTP 상태코드 반영)을 제공합니다.
- **N+1 완화**: `hibernate.default_batch_fetch_size` 설정으로 지연 로딩 시 IN 절 배치 조회를 적용했습니다.
- **문서화**: `springdoc-openapi`(Swagger UI) — 실행 후 `/swagger-ui.html` 접속.

---

## 📖 코드 읽는 순서 (구조 이해용)

처음 보는 사람이 이 프로젝트를 이해하기 좋은 순서입니다. 핵심 원리는 **공통 부품 → 데이터 클래스 → 기능 하나를 전 계층으로 → 같은 패턴 반복 → 공통 처리**. 한 기능이 `Controller → Service → Repository → Entity → DB`로 흐르는 걸 한 번 체득하면 나머지는 반복입니다.

### 0. 건너뛰기 (인프라)
`build.gradle`, `gradlew*`, `.gitattributes` 등 — 코드 이해엔 불필요. (`build.gradle`만 의존성 확인용)

### 1. 설정 · 공통 부품
1. `resources/application.yml` — DB 연결 · `ddl-auto`
2. `test/resources/application.yml` — 테스트용 H2 인메모리
3. `domain/BaseEntity` — 생성/수정 시각 공통 매핑
4. `common/domain/ActiveStatus` → `ActiveStatusConverter` — enum ↔ DB 소문자 변환
5. `common/dto/ApiResponse`, `SuccessStatus`, `dto/response/PageRes` — 응답 공통 포장 · 페이징

### 2. 데이터 클래스 (엔티티 + enum/컨버터)
필드와 연관관계(`@ManyToOne` / `@OneToMany`) 위주로. 의존 순서:
`Member` → `Category` → `Restaurant` → `Menu` → `OptionGroup` → `MenuOption` → `Address` → `Order` → `OrderItem` → `OrderItemOption`
(곁들여: `AddressType`, `OrderStatus`, `SelectionType` + 각 Converter)

### 3. 기능 하나를 전 계층으로 — 가장 단순한 Category ⭐
요청이 들어와 응답으로 나가는 흐름을 한 줄기로 따라가기:
`CategoryController`(입구) → `CategoryCreateReq`(입력 · `@Valid`) → `CategoryService`(`@Transactional` · 로직) → `CategoryRepository`(DB) → `CategoryRes`(출력)

### 4. 같은 패턴 반복 (난이도 순)
각 묶음 모두 `Controller → 요청 DTO → Service → Repository → 응답 DTO` 구조:
- **Member** — 회원가입/로그인 (중복 검사)
- **Restaurant** — 페이징 + 카테고리 FK (`RestaurantDetailRes`)
- **Menu** — `cascade`로 옵션 그룹·옵션 동시 저장
- **Order** — 가장 복잡: 여러 FK + 항목/옵션 조립 + 가격 스냅샷
- **Address** — 회원 주소 (복습용)

### 5. 공통 예외 처리 · 문서
- `exception/errorcode/ErrorStatus` → `GeneralException` + 도메인별 `*Exception` → `handler/GlobalExceptionHandler` — 예외가 일관된 JSON 에러로 변환되는 흐름
- `config/SwaggerConfig` — API 문서 자동화

---

## API 문서화

구현한 API를 Postman 또는 Swagger로 문서화한 뒤 URL을 노션 미션 페이지에 제출하세요.