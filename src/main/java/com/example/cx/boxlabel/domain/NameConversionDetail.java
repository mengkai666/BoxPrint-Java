package com.example.cx.boxlabel.domain;

import java.util.ArrayList;
import java.util.List;

public class NameConversionDetail {

    private String productConfigId;
    private String inventoryCode;
    private String inventoryName;
    private String brandName;
    private ProductTemplateBinding templateBinding;
    private List<NameConversionFieldSource> fieldSources = new ArrayList<NameConversionFieldSource>();

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

    public ProductTemplateBinding getTemplateBinding() {
        return templateBinding;
    }

    public void setTemplateBinding(ProductTemplateBinding templateBinding) {
        this.templateBinding = templateBinding;
    }

    public List<NameConversionFieldSource> getFieldSources() {
        return fieldSources;
    }

    public void setFieldSources(List<NameConversionFieldSource> fieldSources) {
        this.fieldSources = fieldSources == null ? new ArrayList<NameConversionFieldSource>() : fieldSources;
    }
}
