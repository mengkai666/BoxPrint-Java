package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.BoxLabelPrintJobRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobResponse;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.RenderedLabel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcPrintJobRepository implements PrintJobRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPrintJobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BoxLabelPrintJobResponse recordBrowserPrint(BoxLabelPrintJobRequest request, RenderedLabel renderedLabel) {
        String jobId = UUID.randomUUID().toString();
        String labelType = request.getLabelType() == null || request.getLabelType().trim().isEmpty()
                ? "BOX"
                : request.getLabelType().trim().toUpperCase();
        jdbcTemplate.update(
                "INSERT INTO LP_PRINT_JOB (job_id, product_config_id, label_type, template_code, template_version, " +
                        "output_format, output_file_id, output_preview_url, output_size_bytes, printer_name, copies, " +
                        "operator_name, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                jobId,
                request.getProductConfigId(),
                labelType,
                renderedLabel.getTemplateCode(),
                renderedLabel.getTemplateVersion(),
                renderedLabel.getFormat().name(),
                renderedLabel.getFileId(),
                renderedLabel.getPreviewUrl(),
                renderedLabel.getSizeBytes(),
                request.getPrinterName(),
                request.getCopies(),
                request.getOperator(),
                "BROWSER_PRINT_READY"
        );
        return findByJobId(jobId);
    }

    @Override
    public List<BoxLabelPrintJobResponse> listRecent() {
        return jdbcTemplate.query(
                "SELECT job_id, product_config_id, label_type, status, printer_name, copies, operator_name, " +
                        "output_file_id, output_format, output_preview_url, template_code, template_version, output_size_bytes " +
                        "FROM LP_PRINT_JOB ORDER BY created_at DESC, job_id DESC",
                jobMapper()
        );
    }

    private BoxLabelPrintJobResponse findByJobId(String jobId) {
        return jdbcTemplate.queryForObject(
                "SELECT job_id, product_config_id, label_type, status, printer_name, copies, operator_name, " +
                        "output_file_id, output_format, output_preview_url, template_code, template_version, output_size_bytes " +
                        "FROM LP_PRINT_JOB WHERE job_id = ?",
                jobMapper(),
                jobId
        );
    }

    private RowMapper<BoxLabelPrintJobResponse> jobMapper() {
        return new RowMapper<BoxLabelPrintJobResponse>() {
            @Override
            public BoxLabelPrintJobResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                RenderedLabel output = new RenderedLabel();
                output.setFileId(rs.getString("output_file_id"));
                output.setFormat(LabelOutputFormat.valueOf(rs.getString("output_format")));
                output.setPreviewUrl(rs.getString("output_preview_url"));
                output.setTemplateCode(rs.getString("template_code"));
                output.setTemplateVersion(rs.getString("template_version"));
                output.setSizeBytes(rs.getLong("output_size_bytes"));

                BoxLabelPrintJobResponse response = new BoxLabelPrintJobResponse();
                response.setSuccess(true);
                response.setJobId(rs.getString("job_id"));
                response.setProductConfigId(rs.getString("product_config_id"));
                response.setLabelType(rs.getString("label_type"));
                response.setStatus(rs.getString("status"));
                response.setPrinterName(rs.getString("printer_name"));
                response.setCopies(rs.getInt("copies"));
                response.setOperator(rs.getString("operator_name"));
                response.setOutput(output);
                return response;
            }
        };
    }
}
