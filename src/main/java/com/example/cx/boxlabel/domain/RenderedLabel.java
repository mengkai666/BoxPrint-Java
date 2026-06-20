package com.example.cx.boxlabel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.file.Path;

public class RenderedLabel {

    private String fileId;
    private LabelOutputFormat format;
    private String previewUrl;
    private String templateCode;
    private String templateVersion;
    private long sizeBytes;

    @JsonIgnore
    private Path filePath;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public LabelOutputFormat getFormat() {
        return format;
    }

    public void setFormat(LabelOutputFormat format) {
        this.format = format;
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

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
