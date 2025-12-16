package com.m1raynee.db.converters;

import com.m1raynee.db.enums.ActionState;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionStateConverter implements AttributeConverter<ActionState, String> {
    @Override
    public String convertToDatabaseColumn(ActionState category) {
        if (category == null)
            return null;
        return category.getCode();
    }

    @Override
    public ActionState convertToEntityAttribute(String dbCode) {
        if (dbCode == null)
            return null;
        return ActionState.fromCode(dbCode);
    }
}
