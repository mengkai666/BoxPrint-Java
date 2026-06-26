package com.example.cx.boxlabel.domain;

public class LabelTemplateElement {

    private String id;
    private int sortOrder;
    private String type;
    private String text;
    private String fieldName;
    private double leftMm;
    private double topMm;
    private double widthMm;
    private double heightMm;
    private int fontSize = 10;
    private boolean bold;
    private String textAlign = "left";
    private boolean locked;
    private String visibleWhenField;
    private String visibleWhenOperator;
    private String visibleWhenValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public double getLeftMm() {
        return leftMm;
    }

    public void setLeftMm(double leftMm) {
        this.leftMm = leftMm;
    }

    public double getTopMm() {
        return topMm;
    }

    public void setTopMm(double topMm) {
        this.topMm = topMm;
    }

    public double getWidthMm() {
        return widthMm;
    }

    public void setWidthMm(double widthMm) {
        this.widthMm = widthMm;
    }

    public double getHeightMm() {
        return heightMm;
    }

    public void setHeightMm(double heightMm) {
        this.heightMm = heightMm;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public String getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getVisibleWhenField() {
        return visibleWhenField;
    }

    public void setVisibleWhenField(String visibleWhenField) {
        this.visibleWhenField = visibleWhenField;
    }

    public String getVisibleWhenOperator() {
        return visibleWhenOperator;
    }

    public void setVisibleWhenOperator(String visibleWhenOperator) {
        this.visibleWhenOperator = visibleWhenOperator;
    }

    public String getVisibleWhenValue() {
        return visibleWhenValue;
    }

    public void setVisibleWhenValue(String visibleWhenValue) {
        this.visibleWhenValue = visibleWhenValue;
    }
}
