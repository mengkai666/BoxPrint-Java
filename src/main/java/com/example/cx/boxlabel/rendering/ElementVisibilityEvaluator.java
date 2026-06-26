package com.example.cx.boxlabel.rendering;

import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.LabelTemplateElement;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class ElementVisibilityEvaluator {

    public boolean isVisible(LabelTemplateElement element, BoxLabelPrintRow row) {
        if (element == null) {
            return false;
        }
        String field = trimToNull(element.getVisibleWhenField());
        String operator = trimToNull(element.getVisibleWhenOperator());
        if (field == null || operator == null) {
            return true;
        }
        String actual = fieldValue(row, field);
        String expected = trimToEmpty(element.getVisibleWhenValue());
        String normalizedOperator = operator.toLowerCase();
        if ("empty".equals(normalizedOperator)) {
            return actual.trim().isEmpty();
        }
        if ("notempty".equals(normalizedOperator) || "not_empty".equals(normalizedOperator)) {
            return !actual.trim().isEmpty();
        }
        if ("equals".equals(normalizedOperator)) {
            return actual.equals(expected);
        }
        if ("notequals".equals(normalizedOperator) || "not_equals".equals(normalizedOperator)) {
            return !actual.equals(expected);
        }
        if ("contains".equals(normalizedOperator)) {
            return actual.contains(expected);
        }
        if ("startswith".equals(normalizedOperator) || "starts_with".equals(normalizedOperator)) {
            return actual.startsWith(expected);
        }
        if ("endswith".equals(normalizedOperator) || "ends_with".equals(normalizedOperator)) {
            return actual.endsWith(expected);
        }
        return true;
    }

    private String fieldValue(BoxLabelPrintRow row, String field) {
        if (row == null) {
            return "";
        }
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(field, BoxLabelPrintRow.class);
            Method readMethod = descriptor.getReadMethod();
            Object value = readMethod == null ? null : readMethod.invoke(row);
            return value == null ? "" : String.valueOf(value);
        } catch (Exception e) {
            return "";
        }
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
