package com.kuit.baemin.domain.menu;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * SelectionType ↔ DB 문자열 변환기.
 * ERD: option_group.selection_type ENUM('single','multiple') — 소문자 저장/복원.
 */
@Converter(autoApply = true)
public class SelectionTypeConverter implements AttributeConverter<SelectionType, String> {

    @Override
    public String convertToDatabaseColumn(SelectionType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(); // SINGLE → "single"
    }

    @Override
    public SelectionType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SelectionType.valueOf(dbData.toUpperCase()); // "single" → SINGLE
    }
}
