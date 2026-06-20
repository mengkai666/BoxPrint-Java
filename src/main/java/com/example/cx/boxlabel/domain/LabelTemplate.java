package com.example.cx.boxlabel.domain;

public class LabelTemplate {

    private String code;
    private String name;
    private String fileName;
    private String version;
    private String engine;
    private String labelType;
    private String status;
    private String importSource;
    private double pageWidthMm;
    private double pageHeightMm;
    private boolean enabled;
    private java.util.List<LabelTemplateElement> elements = new java.util.ArrayList<LabelTemplateElement>();

    public static LabelTemplate enabled(String code, String name, String fileName, String version) {
        LabelTemplate template = new LabelTemplate();
        template.setCode(code);
        template.setName(name);
        template.setFileName(fileName);
        template.setVersion(version);
        template.setEngine("JASPER");
        template.setLabelType("BOX");
        template.setStatus("ACTIVE");
        template.setImportSource("BUILT_IN");
        template.setPageWidthMm(120);
        template.setPageHeightMm(80);
        template.setEnabled(true);
        return template;
    }

    public static LabelTemplate configLayout(String code, String name, String labelType, double pageWidthMm, double pageHeightMm) {
        LabelTemplate template = new LabelTemplate();
        template.setCode(code);
        template.setName(name);
        template.setVersion("1");
        template.setEngine("CONFIG_LAYOUT");
        template.setLabelType(labelType);
        template.setStatus("DRAFT");
        template.setImportSource("MANUAL");
        template.setPageWidthMm(pageWidthMm);
        template.setPageHeightMm(pageHeightMm);
        template.setEnabled(true);
        return template;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getLabelType() {
        return labelType;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImportSource() {
        return importSource;
    }

    public void setImportSource(String importSource) {
        this.importSource = importSource;
    }

    public double getPageWidthMm() {
        return pageWidthMm;
    }

    public void setPageWidthMm(double pageWidthMm) {
        this.pageWidthMm = pageWidthMm;
    }

    public double getPageHeightMm() {
        return pageHeightMm;
    }

    public void setPageHeightMm(double pageHeightMm) {
        this.pageHeightMm = pageHeightMm;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public java.util.List<LabelTemplateElement> getElements() {
        return elements;
    }

    public void setElements(java.util.List<LabelTemplateElement> elements) {
        this.elements = elements == null
                ? new java.util.ArrayList<LabelTemplateElement>()
                : new java.util.ArrayList<LabelTemplateElement>(elements);
    }

    public LabelTemplate copy() {
        LabelTemplate copy = new LabelTemplate();
        copy.setCode(code);
        copy.setName(name);
        copy.setFileName(fileName);
        copy.setVersion(version);
        copy.setEngine(engine);
        copy.setLabelType(labelType);
        copy.setStatus(status);
        copy.setImportSource(importSource);
        copy.setPageWidthMm(pageWidthMm);
        copy.setPageHeightMm(pageHeightMm);
        copy.setEnabled(enabled);
        copy.setElements(elements);
        return copy;
    }
}
