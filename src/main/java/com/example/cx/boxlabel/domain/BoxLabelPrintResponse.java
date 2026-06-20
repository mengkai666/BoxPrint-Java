package com.example.cx.boxlabel.domain;

public class BoxLabelPrintResponse {

    private boolean success;
    private String mode;
    private String message;
    private String templateCode;
    private String templateVersion;
    private BoxLabelPrintRow row;

    public static BoxLabelPrintResponse success(String mode, String message, LabelTemplate template, BoxLabelPrintRow row) {
        BoxLabelPrintResponse response = new BoxLabelPrintResponse();
        response.setSuccess(true);
        response.setMode(mode);
        response.setMessage(message);
        response.setTemplateCode(template.getCode());
        response.setTemplateVersion(template.getVersion());
        response.setRow(row);
        return response;
    }

    public static BoxLabelPrintResponse failure(String mode, String message) {
        BoxLabelPrintResponse response = new BoxLabelPrintResponse();
        response.setSuccess(false);
        response.setMode(mode);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public BoxLabelPrintRow getRow() {
        return row;
    }

    public void setRow(BoxLabelPrintRow row) {
        this.row = row;
    }
}
