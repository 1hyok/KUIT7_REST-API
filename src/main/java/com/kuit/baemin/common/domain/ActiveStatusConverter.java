package com.kuit.baemin.common.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ActiveStatus ↔ DB 문자열 변환기.
 * ERD의 status 컬럼이 소문자 ENUM('active','inactive') 이므로,
 * 자바 enum 상수(대문자)를 DB에는 소문자로 저장하고, 읽을 때 다시 enum 으로 복원한다.
 * autoApply=true → ActiveStatus 타입 필드 전체에 자동 적용
 * (그 필드에 @Enumerated 금지 — 컨버터와 @Enumerated 는 같은 필드의 "저장 방식"을 정하는 둘 중 택1 방식이라 동시에 쓰면 충돌, JPA 가 금함)
 */
@Converter(autoApply = true)                                                  // autoApply=true → "이 컨버터(=위 변환)"를 ActiveStatus 타입인 모든 필드에 자동 적용 (각 필드에 @Convert 일일이 안 붙여도 됨)
public class ActiveStatusConverter implements AttributeConverter<ActiveStatus, String> {  // <자바 타입, DB 타입> 순서

    // INSERT/UPDATE 시 자바 → DB 방향: 자바 enum 값을 DB에 저장할 문자열로 변환
    @Override
    public String convertToDatabaseColumn(ActiveStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(); // ACTIVE → "active" (null 은 그대로 null 저장)
    }

    // SELECT 시 DB → 자바 방향: DB 에서 읽은 문자열을 자바 enum 으로 복원
    @Override
    public ActiveStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ActiveStatus.valueOf(dbData.toUpperCase()); // "active" → ACTIVE (대문자 enum 상수명과 매칭)
    }
}
