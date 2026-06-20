package com.example.cx.boxlabel.domain;

import javax.validation.constraints.Min;

public class BoxLabelPrintJobRequest extends BoxLabelRenderRequest {

    private String printerName;

    @Min(1)
    private int copies = 1;

    private String operator;

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
