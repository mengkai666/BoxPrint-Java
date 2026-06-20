package com.example.cx.boxlabel.domain;

public class BoxLabelProductSummary {

    private String productConfigId;
    private String inventoryCode;
    private String inventoryName;
    private String brandName;
    private String boxTemplateCode;
    private String boxTemplateName;

    public String getProductConfigId() {
        return productConfigId;
    }

    public void setProductConfigId(String productConfigId) {
        this.productConfigId = productConfigId;
    }

    public String getInventoryCode() {
        return inventoryCode;
    }

    public void setInventoryCode(String inventoryCode) {
        this.inventoryCode = inventoryCode;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBoxTemplateCode() {
        return boxTemplateCode;
    }

    public void setBoxTemplateCode(String boxTemplateCode) {
        this.boxTemplateCode = boxTemplateCode;
    }

    public String getBoxTemplateName() {
        return boxTemplateName;
    }

    public void setBoxTemplateName(String boxTemplateName) {
        this.boxTemplateName = boxTemplateName;
    }
}
