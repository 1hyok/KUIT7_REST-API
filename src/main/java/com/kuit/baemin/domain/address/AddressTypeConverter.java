package com.kuit.baemin.domain.address;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * AddressType ↔ DB 문자열 변환기.
 * ERD: address.type ENUM('home','work','etc') — 소문자 저장/복원.
 */
@Converter(autoApply = true)
public class AddressTypeConverter implements AttributeConverter<AddressType, String> {

    @Override
    public String convertToDatabaseColumn(AddressType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(); // HOME → "home"
    }

    @Override
    public AddressType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AddressType.valueOf(dbData.toUpperCase()); // "home" → HOME
    }
}
