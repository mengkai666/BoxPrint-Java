package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.LabelTemplateElement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcLabelTemplateRepository implements LabelTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcLabelTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<LabelTemplate> findAll() {
        List<LabelTemplate> templates = jdbcTemplate.query(
                "SELECT template_code, template_name, label_type, engine, file_name, version_no, " +
                        "status, import_source, page_width_mm, page_height_mm, enabled " +
                        "FROM LP_LABEL_TEMPLATE ORDER BY display_order, template_code",
                templateMapper()
        );
        for (LabelTemplate template : templates) {
            template.setElements(findElements(template.getCode()));
        }
        return templates;
    }

    @Override
    public LabelTemplate findByCode(String templateCode) {
        try {
            LabelTemplate template = jdbcTemplate.queryForObject(
                    "SELECT template_code, template_name, label_type, engine, file_name, version_no, " +
                            "status, import_source, page_width_mm, page_height_mm, enabled " +
                            "FROM LP_LABEL_TEMPLATE WHERE template_code = ?",
                    templateMapper(),
                    templateCode
            );
            template.setElements(findElements(template.getCode()));
            return template;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(LabelTemplate template) {
        if (exists(template.getCode())) {
            jdbcTemplate.update(
                    "UPDATE LP_LABEL_TEMPLATE SET template_name = ?, label_type = ?, engine = ?, file_name = ?, " +
                            "version_no = ?, status = ?, import_source = ?, page_width_mm = ?, page_height_mm = ?, " +
                            "enabled = ?, updated_at = CURRENT_TIMESTAMP WHERE template_code = ?",
                    template.getName(),
                    template.getLabelType(),
                    template.getEngine(),
                    template.getFileName(),
                    template.getVersion(),
                    template.getStatus(),
                    template.getImportSource(),
                    template.getPageWidthMm(),
                    template.getPageHeightMm(),
                    template.isEnabled(),
                    template.getCode()
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO LP_LABEL_TEMPLATE (template_code, template_name, label_type, engine, file_name, " +
                            "version_no, status, import_source, page_width_mm, page_height_mm, enabled, display_order) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    template.getCode(),
                    template.getName(),
                    template.getLabelType(),
                    template.getEngine(),
                    template.getFileName(),
                    template.getVersion(),
                    template.getStatus(),
                    template.getImportSource(),
                    template.getPageWidthMm(),
                    template.getPageHeightMm(),
                    template.isEnabled(),
                    1000
            );
        }
    }

    @Override
    public void replaceElements(String templateCode, List<LabelTemplateElement> elements) {
        jdbcTemplate.update("DELETE FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ?", templateCode);
        for (LabelTemplateElement element : elements) {
            jdbcTemplate.update(
                    "INSERT INTO LP_LABEL_TEMPLATE_ELEMENT (element_id, template_code, sort_order, element_type, " +
                            "text_value, field_name, left_mm, top_mm, width_mm, height_mm, font_size, bold) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    element.getId(),
                    templateCode,
                    element.getSortOrder(),
                    element.getType(),
                    element.getText(),
                    element.getFieldName(),
                    element.getLeftMm(),
                    element.getTopMm(),
                    element.getWidthMm(),
                    element.getHeightMm(),
                    element.getFontSize(),
                    element.isBold()
            );
        }
    }

    @Override
    public void recordImport(String importId,
                             String importSource,
                             String templateCode,
                             String labelType,
                             String legacyTemplateId,
                             String legacyTemplateName,
                             String sampleName,
                             String status,
                             String message) {
        jdbcTemplate.update(
                "INSERT INTO LP_TEMPLATE_IMPORT_LOG (import_id, import_source, template_code, label_type, " +
                        "legacy_template_id, legacy_template_name, sample_name, status, message) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                importId,
                importSource,
                templateCode,
                labelType,
                legacyTemplateId,
                legacyTemplateName,
                sampleName,
                status,
                message
        );
    }

    private boolean exists(String templateCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LP_LABEL_TEMPLATE WHERE template_code = ?",
                Integer.class,
                templateCode
        );
        return count != null && count > 0;
    }

    private List<LabelTemplateElement> findElements(String templateCode) {
        return jdbcTemplate.query(
                "SELECT element_id, sort_order, element_type, text_value, field_name, left_mm, top_mm, " +
                        "width_mm, height_mm, font_size, bold " +
                        "FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ? ORDER BY sort_order, element_id",
                elementMapper(),
                templateCode
        );
    }

    private RowMapper<LabelTemplate> templateMapper() {
        return new RowMapper<LabelTemplate>() {
            @Override
            public LabelTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
                LabelTemplate template = new LabelTemplate();
                template.setCode(rs.getString("template_code"));
                template.setName(rs.getString("template_name"));
                template.setLabelType(rs.getString("label_type"));
                template.setEngine(rs.getString("engine"));
                template.setFileName(rs.getString("file_name"));
                template.setVersion(rs.getString("version_no"));
                template.setStatus(rs.getString("status"));
                template.setImportSource(rs.getString("import_source"));
                template.setPageWidthMm(rs.getDouble("page_width_mm"));
                template.setPageHeightMm(rs.getDouble("page_height_mm"));
                template.setEnabled(rs.getBoolean("enabled"));
                template.setElements(new ArrayList<LabelTemplateElement>());
                return template;
            }
        };
    }

    private RowMapper<LabelTemplateElement> elementMapper() {
        return new RowMapper<LabelTemplateElement>() {
            @Override
            public LabelTemplateElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                LabelTemplateElement element = new LabelTemplateElement();
                element.setId(rs.getString("element_id"));
                element.setSortOrder(rs.getInt("sort_order"));
                element.setType(rs.getString("element_type"));
                element.setText(rs.getString("text_value"));
                element.setFieldName(rs.getString("field_name"));
                element.setLeftMm(rs.getDouble("left_mm"));
                element.setTopMm(rs.getDouble("top_mm"));
                element.setWidthMm(rs.getDouble("width_mm"));
                element.setHeightMm(rs.getDouble("height_mm"));
                element.setFontSize(rs.getInt("font_size"));
                element.setBold(rs.getBoolean("bold"));
                return element;
            }
        };
    }
}
