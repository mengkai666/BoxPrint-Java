package com.example.cx.boxlabel.domain;

public class ProductTemplateBinding {

    private String productConfigId;
    private String boxTemplateCode;
    private String bagTemplateCode;
    private String source = "JAVA_BINDING";

    public String getProductConfigId() {
        return productConfigId;
    }

    public void setProductConfigId(String productConfigId) {
        this.productConfigId = productConfigId;
    }

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
