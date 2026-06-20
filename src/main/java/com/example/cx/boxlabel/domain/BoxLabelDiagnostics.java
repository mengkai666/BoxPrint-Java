package com.example.cx.boxlabel.domain;

import java.util.ArrayList;
import java.util.List;

public class BoxLabelDiagnostics {

    private String productConfigId;
    private boolean templateAvailable;
    private boolean logoAvailable;
    private boolean barcodeValid;
    private List<String> missingRequiredFields = new ArrayList<String>();
    private List<String> warnings = new ArrayList<String>();

    public String getProductConfigId() {
        return productConfigId;
    }

    public void setProductConfigId(String productConfigId) {
        this.productConfigId = productConfigId;
    }

    public boolean isTemplateAvailable() {
        return templateAvailable;
    }

    public void setTemplateAvailable(boolean templateAvailable) {
        this.templateAvailable = templateAvailable;
    }

    public boolean isLogoAvailable() {
        return logoAvailable;
    }

    public void setLogoAvailable(boolean logoAvailable) {
        this.logoAvailable = logoAvailable;
    }

    public boolean isBarcodeValid() {
        return barcodeValid;
    }

    public void setBarcodeValid(boolean barcodeValid) {
        this.barcodeValid = barcodeValid;
    }

    public List<String> getMissingRequiredFields() {
        return missingRequiredFields;
    }

    public void setMissingRequiredFields(List<String> missingRequiredFields) {
        this.missingRequiredFields = missingRequiredFields;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
