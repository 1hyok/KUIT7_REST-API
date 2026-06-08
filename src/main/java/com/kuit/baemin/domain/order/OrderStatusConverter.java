package com.kuit.baemin.domain.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * OrderStatus ↔ DB 문자열 변환기.
 * ERD: orders.order_status ENUM('pending','accepted','cooking','delivering','completed','canceled') — 소문자 저장/복원.
 */
@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(); // PENDING → "pending"
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OrderStatus.valueOf(dbData.toUpperCase()); // "pending" → PENDING
    }
}
