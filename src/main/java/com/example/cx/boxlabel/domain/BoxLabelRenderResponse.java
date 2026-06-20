package com.example.cx.boxlabel.domain;

public class BoxLabelRenderResponse {

    private boolean success;
    private String message;
    private String fileId;
    private String previewUrl;
    private String templateCode;
    private String templateVersion;
    private LabelOutputFormat format;
    private long sizeBytes;

    public static BoxLabelRenderResponse from(RenderedLabel renderedLabel) {
        BoxLabelRenderResponse response = new BoxLabelRenderResponse();
        response.setSuccess(true);
        response.setMessage("Label rendered by pure Java JasperReports renderer.");
        response.setFileId(renderedLabel.getFileId());
        response.setPreviewUrl(renderedLabel.getPreviewUrl());
        response.setTemplateCode(renderedLabel.getTemplateCode());
        response.setTemplateVersion(renderedLabel.getTemplateVersion());
        response.setFormat(renderedLabel.getFormat());
        response.setSizeBytes(renderedLabel.getSizeBytes());
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
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

    public LabelOutputFormat getFormat() {
        return format;
    }

    public void setFormat(LabelOutputFormat format) {
        this.format = format;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
