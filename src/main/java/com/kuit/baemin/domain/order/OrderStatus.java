package com.kuit.baemin.domain.order;

/**
 * 주문 진행 상태를 나타내는 enum (ERD: orders.order_status 컬럼).
 *
 * <p>enum = 미리 정해 둔 값들만 골라 쓸 수 있는 타입. 즉 주문 상태는 아래 6가지 중 하나만 가질 수 있다.
 * 보통 주문은 PENDING -> ACCEPTED -> COOKING -> DELIVERING -> COMPLETED 순서로 흐르고,
 * 중간에 끊기면 CANCELED 가 된다.</p>
 *
 * <p>아래 isCancelable()/canProceedTo() 처럼 "지금 상태에서 무엇이 가능한지"를 판단하는 로직을
 * enum 안에 두면, 상태와 관련된 규칙을 한곳에서 관리할 수 있다.</p>
 */
public enum OrderStatus {
    PENDING,     // 접수 대기
    ACCEPTED,    // 접수 완료
    COOKING,     // 조리 중
    DELIVERING,  // 배달 중
    COMPLETED,   // 배달 완료
    CANCELED;    // 취소됨

    /** 취소 가능한 상태인지 (접수 전/직후까지만 취소 허용) */
    public boolean isCancelable() {
        return this == PENDING || this == ACCEPTED;
    }

    /**
     * (가게/관리자용) 진행 상태를 '바로 다음 단계'로만 바꿀 수 있는지 검사.
     * 정방향 한 단계(PENDING→ACCEPTED→COOKING→DELIVERING→COMPLETED)만 허용 — 역방향·건너뛰기·취소(CANCELED)는 불가.
     * (취소는 cancel() 경로에서 isCancelable() 로 따로 처리)
     */
    public boolean canProceedTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == ACCEPTED;
            case ACCEPTED   -> next == COOKING;
            case COOKING    -> next == DELIVERING;
            case DELIVERING -> next == COMPLETED;
            default         -> false;   // COMPLETED·CANCELED 는 더 진행 불가
        };
    }
}
