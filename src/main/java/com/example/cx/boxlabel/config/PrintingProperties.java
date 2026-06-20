package com.example.cx.boxlabel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cx.printing")
public class PrintingProperties {

    private String templatePath = "src/main/resources/reports";
    private String logoPath = "src/main/resources/static/logo";
    private String outputPath = "target/boxlabel-output";
    private String sqlServerUrl;
    private String sqlServerUsername;
    private String sqlServerPassword;
    private String u8DatabaseName = "UFDATA_001_2018";
    private int fileRetentionHours = 24;

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getSqlServerUrl() {
        return sqlServerUrl;
    }

    public void setSqlServerUrl(String sqlServerUrl) {
        this.sqlServerUrl = sqlServerUrl;
    }

    public String getSqlServerUsername() {
        return sqlServerUsername;
    }

    public void setSqlServerUsername(String sqlServerUsername) {
        this.sqlServerUsername = sqlServerUsername;
    }

    public String getSqlServerPassword() {
        return sqlServerPassword;
    }

    public void setSqlServerPassword(String sqlServerPassword) {
        this.sqlServerPassword = sqlServerPassword;
    }

    public String getU8DatabaseName() {
        return u8DatabaseName;
    }

    public void setU8DatabaseName(String u8DatabaseName) {
        this.u8DatabaseName = u8DatabaseName;
    }

    public int getFileRetentionHours() {
        return fileRetentionHours;
    }

    public void setFileRetentionHours(int fileRetentionHours) {
        this.fileRetentionHours = fileRetentionHours;
    }
}
