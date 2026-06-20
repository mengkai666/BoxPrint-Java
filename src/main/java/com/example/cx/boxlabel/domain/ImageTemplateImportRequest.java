package com.example.cx.boxlabel.domain;

public class ImageTemplateImportRequest {

    private String labelType;
    private String sampleName;

    public String getLabelType() {
        return labelType;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
}
