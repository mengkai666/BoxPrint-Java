package com.example.cx.boxlabel.domain;

public class LegacyTemplateImportRequest {

    private String labelType;
    private String legacyTemplateId;
    private String legacyTemplateName;

    public String getLabelType() {
        return labelType;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public String getLegacyTemplateId() {
        return legacyTemplateId;
    }

    public void setLegacyTemplateId(String legacyTemplateId) {
        this.legacyTemplateId = legacyTemplateId;
    }

    public String getLegacyTemplateName() {
        return legacyTemplateName;
    }

    public void setLegacyTemplateName(String legacyTemplateName) {
        this.legacyTemplateName = legacyTemplateName;
    }
}
