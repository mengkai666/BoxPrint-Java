package com.example.cx.boxlabel.domain;

public class NameConversionFieldSource {

    private String fieldName;
    private String displayName;
    private String category;
    private String source;
    private String value;
    private String sourceType;
    private String fieldStatus;
    private String statusText;

    public NameConversionFieldSource() {
    }

    public NameConversionFieldSource(String fieldName, String source, String value) {
        this.fieldName = fieldName;
        this.displayName = fieldName;
        this.category = "基础字段";
        this.source = source;
        this.value = value;
    }

    public NameConversionFieldSource(String category, String displayName, String fieldName, String source, String value) {
        this.category = category;
        this.displayName = displayName;
        this.fieldName = fieldName;
        this.source = source;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getFieldStatus() {
        return fieldStatus;
    }

    public void setFieldStatus(String fieldStatus) {
        this.fieldStatus = fieldStatus;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
