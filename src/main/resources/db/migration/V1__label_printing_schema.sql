CREATE TABLE LP_LABEL_TEMPLATE (
    template_code VARCHAR(80) NOT NULL PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL,
    label_type VARCHAR(20) NOT NULL,
    engine VARCHAR(40) NOT NULL,
    file_name VARCHAR(260),
    version_no VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    import_source VARCHAR(30) NOT NULL,
    page_width_mm DECIMAL(10, 2) NOT NULL,
    page_height_mm DECIMAL(10, 2) NOT NULL,
    enabled BIT NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE LP_LABEL_TEMPLATE_ELEMENT (
    element_id VARCHAR(80) NOT NULL PRIMARY KEY,
    template_code VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL,
    element_type VARCHAR(40) NOT NULL,
    text_value VARCHAR(1000),
    field_name VARCHAR(120),
    left_mm DECIMAL(10, 2) NOT NULL,
    top_mm DECIMAL(10, 2) NOT NULL,
    width_mm DECIMAL(10, 2) NOT NULL,
    height_mm DECIMAL(10, 2) NOT NULL,
    font_size INT NOT NULL,
    bold BIT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LP_ELEMENT_TEMPLATE FOREIGN KEY (template_code)
        REFERENCES LP_LABEL_TEMPLATE(template_code)
);

CREATE TABLE LP_PRODUCT_TEMPLATE_BINDING (
    product_config_id VARCHAR(80) NOT NULL PRIMARY KEY,
    box_template_code VARCHAR(80) NOT NULL,
    bag_template_code VARCHAR(80) NOT NULL,
    source VARCHAR(40) NOT NULL,
    legacy_box_template_id VARCHAR(80),
    legacy_bag_template_id VARCHAR(80),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LP_BINDING_BOX_TEMPLATE FOREIGN KEY (box_template_code)
        REFERENCES LP_LABEL_TEMPLATE(template_code),
    CONSTRAINT FK_LP_BINDING_BAG_TEMPLATE FOREIGN KEY (bag_template_code)
        REFERENCES LP_LABEL_TEMPLATE(template_code)
);

CREATE TABLE LP_PRINT_JOB (
    job_id VARCHAR(80) NOT NULL PRIMARY KEY,
    product_config_id VARCHAR(80),
    label_type VARCHAR(20) NOT NULL,
    template_code VARCHAR(80) NOT NULL,
    template_version VARCHAR(40) NOT NULL,
    output_format VARCHAR(20) NOT NULL,
    output_file_id VARCHAR(80) NOT NULL,
    output_preview_url VARCHAR(260) NOT NULL,
    output_size_bytes BIGINT NOT NULL,
    printer_name VARCHAR(200),
    copies INT NOT NULL,
    operator_name VARCHAR(120),
    status VARCHAR(40) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE LP_TEMPLATE_IMPORT_LOG (
    import_id VARCHAR(80) NOT NULL PRIMARY KEY,
    import_source VARCHAR(30) NOT NULL,
    template_code VARCHAR(80) NOT NULL,
    label_type VARCHAR(20) NOT NULL,
    legacy_template_id VARCHAR(80),
    legacy_template_name VARCHAR(200),
    sample_name VARCHAR(260),
    status VARCHAR(40) NOT NULL,
    message VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LP_IMPORT_TEMPLATE FOREIGN KEY (template_code)
        REFERENCES LP_LABEL_TEMPLATE(template_code)
);

CREATE INDEX IX_LP_TEMPLATE_LABEL_TYPE ON LP_LABEL_TEMPLATE(label_type, enabled);
CREATE INDEX IX_LP_ELEMENT_TEMPLATE ON LP_LABEL_TEMPLATE_ELEMENT(template_code, sort_order);
CREATE INDEX IX_LP_PRINT_JOB_CREATED ON LP_PRINT_JOB(created_at);
CREATE INDEX IX_LP_IMPORT_CREATED ON LP_TEMPLATE_IMPORT_LOG(created_at);

INSERT INTO LP_LABEL_TEMPLATE (
    template_code, template_name, label_type, engine, file_name, version_no,
    status, import_source, page_width_mm, page_height_mm, enabled, display_order
) VALUES (
    'box-standard', 'Box label standard', 'BOX', 'JASPER', 'box-label-standard.jrxml', '1.0',
    'ACTIVE', 'BUILT_IN', 120, 80, 1, 10
);

INSERT INTO LP_LABEL_TEMPLATE (
    template_code, template_name, label_type, engine, file_name, version_no,
    status, import_source, page_width_mm, page_height_mm, enabled, display_order
) VALUES (
    'box-config-standard', 'Box configurable standard', 'BOX', 'CONFIG_LAYOUT', NULL, '1',
    'ACTIVE', 'BUILT_IN', 120, 80, 1, 15
);

INSERT INTO LP_LABEL_TEMPLATE (
    template_code, template_name, label_type, engine, file_name, version_no,
    status, import_source, page_width_mm, page_height_mm, enabled, display_order
) VALUES (
    'bag-standard', 'Bag label standard', 'BAG', 'CONFIG_LAYOUT', NULL, '1',
    'ACTIVE', 'BUILT_IN', 80, 50, 1, 20
);

INSERT INTO LP_LABEL_TEMPLATE_ELEMENT (
    element_id, template_code, sort_order, element_type, text_value, field_name,
    left_mm, top_mm, width_mm, height_mm, font_size, bold
) VALUES
    ('box-config-border', 'box-config-standard', 1, 'RECTANGLE', NULL, NULL, 2, 2, 116, 76, 10, 0),
    ('box-config-title', 'box-config-standard', 2, 'STATIC_TEXT', 'Box Label', NULL, 4, 4, 45, 7, 14, 1),
    ('box-config-logo', 'box-config-standard', 3, 'LOGO', NULL, NULL, 92, 4, 22, 14, 10, 0),
    ('box-config-name', 'box-config-standard', 4, 'FIELD_TEXT', NULL, 'boxLabelName', 4, 13, 82, 8, 12, 1),
    ('box-config-brand', 'box-config-standard', 5, 'FIELD_TEXT', NULL, 'brandName', 4, 23, 45, 6, 9, 0),
    ('box-config-spec', 'box-config-standard', 6, 'FIELD_TEXT', NULL, 'packageSpec', 52, 23, 34, 6, 9, 0),
    ('box-config-date', 'box-config-standard', 7, 'FIELD_TEXT', NULL, 'productionDate', 4, 31, 36, 6, 9, 0),
    ('box-config-shelf', 'box-config-standard', 8, 'FIELD_TEXT', NULL, 'shelfLife', 43, 31, 32, 6, 9, 0),
    ('box-config-storage', 'box-config-standard', 9, 'FIELD_TEXT', NULL, 'storageCondition', 4, 39, 80, 6, 8, 0),
    ('box-config-company', 'box-config-standard', 10, 'FIELD_TEXT', NULL, 'company', 4, 47, 80, 5, 8, 0),
    ('box-config-address', 'box-config-standard', 11, 'FIELD_TEXT', NULL, 'address', 4, 53, 86, 5, 7, 0),
    ('box-config-license', 'box-config-standard', 12, 'FIELD_TEXT', NULL, 'productionLicenseNo', 4, 59, 46, 5, 7, 0),
    ('box-config-standard-no', 'box-config-standard', 13, 'FIELD_TEXT', NULL, 'productStandardNo', 52, 59, 34, 5, 7, 0),
    ('box-config-barcode', 'box-config-standard', 14, 'BARCODE', NULL, NULL, 4, 65, 62, 11, 10, 0),
    ('box-config-qrcode', 'box-config-standard', 15, 'QRCODE', NULL, NULL, 92, 52, 22, 22, 10, 0);

INSERT INTO LP_LABEL_TEMPLATE_ELEMENT (
    element_id, template_code, sort_order, element_type, text_value, field_name,
    left_mm, top_mm, width_mm, height_mm, font_size, bold
) VALUES
    ('bag-standard-title', 'bag-standard', 1, 'STATIC_TEXT', 'Bag label', NULL, 4, 4, 70, 8, 14, 1),
    ('bag-standard-name', 'bag-standard', 2, 'FIELD_TEXT', NULL, 'boxLabelName', 4, 14, 90, 8, 12, 0),
    ('bag-standard-spec', 'bag-standard', 3, 'FIELD_TEXT', NULL, 'packageSpec', 4, 24, 70, 7, 10, 0),
    ('bag-standard-barcode', 'bag-standard', 4, 'BARCODE', NULL, NULL, 4, 34, 65, 16, 10, 0),
    ('bag-standard-qrcode', 'bag-standard', 5, 'QRCODE', NULL, NULL, 92, 24, 22, 22, 10, 0);

INSERT INTO LP_PRODUCT_TEMPLATE_BINDING (
    product_config_id, box_template_code, bag_template_code, source,
    legacy_box_template_id, legacy_bag_template_id
) VALUES (
    'DEMO-BOX-001', 'box-standard', 'bag-standard', 'JAVA_BINDING',
    'CD_PRINT_TEMPLATE:box-standard', 'CD_PRINT_TEMPLATE:bag-standard'
);
