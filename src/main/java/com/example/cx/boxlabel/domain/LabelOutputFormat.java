package com.example.cx.boxlabel.domain;

public enum LabelOutputFormat {
    PDF("application/pdf", "pdf"),
    PNG("image/png", "png");

    private final String contentType;
    private final String extension;

    LabelOutputFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }

    public static LabelOutputFormat from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PDF;
        }
        return LabelOutputFormat.valueOf(value.trim().toUpperCase());
    }
}
