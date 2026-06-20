package com.example.cx.boxlabel.domain;

import javax.validation.constraints.NotBlank;

public class LabelTemplateSaveRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String labelType;

    private double pageWidthMm = 120;
    private double pageHeightMm = 80;
    private Boolean enabled;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabelType() {
        return labelType;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public double getPageWidthMm() {
        return pageWidthMm;
    }

    public void setPageWidthMm(double pageWidthMm) {
        this.pageWidthMm = pageWidthMm;
    }

    public double getPageHeightMm() {
        return pageHeightMm;
    }

    public void setPageHeightMm(double pageHeightMm) {
        this.pageHeightMm = pageHeightMm;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
