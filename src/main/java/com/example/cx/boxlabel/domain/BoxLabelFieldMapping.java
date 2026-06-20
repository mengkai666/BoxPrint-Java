package com.example.cx.boxlabel.domain;

public class BoxLabelFieldMapping {

    private String vfpFieldName;
    private String javaFieldName;
    private String javaType;
    private boolean requiredForFrx;
    private String source;
    private String note;

    public BoxLabelFieldMapping() {
    }

    public BoxLabelFieldMapping(String vfpFieldName, String javaFieldName, String javaType,
                                boolean requiredForFrx, String source, String note) {
        this.vfpFieldName = vfpFieldName;
        this.javaFieldName = javaFieldName;
        this.javaType = javaType;
        this.requiredForFrx = requiredForFrx;
        this.source = source;
        this.note = note;
    }

    public String getVfpFieldName() {
        return vfpFieldName;
    }

    public void setVfpFieldName(String vfpFieldName) {
        this.vfpFieldName = vfpFieldName;
    }

    public String getJavaFieldName() {
        return javaFieldName;
    }

    public void setJavaFieldName(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public boolean isRequiredForFrx() {
        return requiredForFrx;
    }

    public void setRequiredForFrx(boolean requiredForFrx) {
        this.requiredForFrx = requiredForFrx;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

