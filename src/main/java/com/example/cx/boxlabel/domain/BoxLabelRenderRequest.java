package com.example.cx.boxlabel.domain;

public class BoxLabelRenderRequest extends BoxLabelPrintRequest {

    private String format = "PDF";

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
