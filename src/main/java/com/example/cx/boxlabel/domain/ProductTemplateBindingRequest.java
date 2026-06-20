package com.example.cx.boxlabel.domain;

public class ProductTemplateBindingRequest {

    private String boxTemplateCode;
    private String bagTemplateCode;

    public String getBoxTemplateCode() {
        return boxTemplateCode;
    }

    public void setBoxTemplateCode(String boxTemplateCode) {
        this.boxTemplateCode = boxTemplateCode;
    }

    public String getBagTemplateCode() {
        return bagTemplateCode;
    }

    public void setBagTemplateCode(String bagTemplateCode) {
        this.bagTemplateCode = bagTemplateCode;
    }
}
