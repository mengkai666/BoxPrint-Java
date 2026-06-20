package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.config.PrintingProperties;
import com.example.cx.boxlabel.domain.BoxLabelDiagnostics;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.ImageTemplateImportRequest;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.LabelTemplateElement;
import com.example.cx.boxlabel.domain.LabelTemplateElementsUpdateRequest;
import com.example.cx.boxlabel.domain.LabelTemplateSaveRequest;
import com.example.cx.boxlabel.domain.LegacyTemplateImportRequest;
import com.example.cx.boxlabel.infrastructure.LabelTemplateRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class BoxLabelTemplateService {

    private final PrintingProperties properties;
    private final LabelTemplateRepository templateRepository;

    public BoxLabelTemplateService(PrintingProperties properties,
                                   LabelTemplateRepository templateRepository) {
        this.properties = properties;
        this.templateRepository = templateRepository;
    }

    public List<LabelTemplate> listTemplates() {
        List<LabelTemplate> result = new ArrayList<LabelTemplate>();
        for (LabelTemplate template : templateRepository.findAll()) {
            result.add(template.copy());
        }
        return Collections.unmodifiableList(result);
    }

    public List<LabelTemplate> listTemplates(String labelType) {
        String expected = normalizeOptionalLabelType(labelType);
        List<LabelTemplate> result = new ArrayList<LabelTemplate>();
        for (LabelTemplate template : templateRepository.findAll()) {
            if (expected == null || expected.equals(normalizeOptionalLabelType(template.getLabelType()))) {
                result.add(template.copy());
            }
        }
        return Collections.unmodifiableList(result);
    }

    public LabelTemplate requireTemplate(String templateCode) {
        String code = templateCode == null || templateCode.trim().isEmpty() ? "box-standard" : templateCode.trim();
        LabelTemplate template = templateRepository.findByCode(code);
        if (template != null && template.isEnabled()) {
            return template.copy();
        }
        throw new IllegalArgumentException("Unknown or disabled label template: " + code);
    }

    public synchronized LabelTemplate saveTemplate(LabelTemplateSaveRequest request) {
        String code = requireCode(request.getCode());
        LabelTemplate template = templateRepository.findByCode(code);
        if (template == null) {
            template = LabelTemplate.configLayout(
                    code,
                    trimToDefault(request.getName(), code),
                    normalizeRequiredLabelType(request.getLabelType()),
                    positiveOrDefault(request.getPageWidthMm(), 120),
                    positiveOrDefault(request.getPageHeightMm(), 80)
            );
        } else {
            template.setName(trimToDefault(request.getName(), template.getName()));
            template.setLabelType(normalizeRequiredLabelType(trimToDefault(request.getLabelType(), template.getLabelType())));
            template.setPageWidthMm(positiveOrDefault(request.getPageWidthMm(), template.getPageWidthMm()));
            template.setPageHeightMm(positiveOrDefault(request.getPageHeightMm(), template.getPageHeightMm()));
            if (!"CONFIG_LAYOUT".equals(template.getEngine())) {
                template.setEngine("CONFIG_LAYOUT");
                template.setFileName(null);
                template.setVersion("1");
                template.setElements(new ArrayList<LabelTemplateElement>());
            }
        }
        template.setStatus(trimToDefault(template.getStatus(), "DRAFT"));
        template.setImportSource(trimToDefault(template.getImportSource(), "MANUAL"));
        template.setEnabled(request.getEnabled() == null || request.getEnabled());
        templateRepository.save(template);
        return templateRepository.findByCode(code).copy();
    }

    public synchronized LabelTemplate updateElements(String templateCode, LabelTemplateElementsUpdateRequest request) {
        LabelTemplate template = requireMutableTemplate(templateCode);
        List<LabelTemplateElement> elements = normalizeElements(request.getElements());
        template.setElements(elements);
        template.setVersion(String.valueOf(parseVersion(template.getVersion()) + 1));
        templateRepository.save(template);
        templateRepository.replaceElements(template.getCode(), elements);
        return templateRepository.findByCode(template.getCode()).copy();
    }

    public synchronized LabelTemplate importFromLegacy(LegacyTemplateImportRequest request) {
        String labelType = normalizeRequiredLabelType(trimToDefault(request.getLabelType(), "BOX"));
        String name = trimToDefault(request.getLegacyTemplateName(), "Legacy template");
        String seed = trimToDefault(request.getLegacyTemplateId(), name).replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
        LabelTemplate template = LabelTemplate.configLayout(uniqueCode("legacy-" + seed), name, labelType, 120, 80);
        template.setImportSource("LEGACY");
        template.setElements(normalizeElements(defaultElements(labelType, name)));
        templateRepository.save(template);
        templateRepository.replaceElements(template.getCode(), template.getElements());
        templateRepository.recordImport(
                UUID.randomUUID().toString(),
                "LEGACY",
                template.getCode(),
                labelType,
                request.getLegacyTemplateId(),
                request.getLegacyTemplateName(),
                null,
                "DRAFT_CREATED",
                "Legacy metadata imported as editable Java template draft."
        );
        return templateRepository.findByCode(template.getCode()).copy();
    }

    public synchronized LabelTemplate importFromImage(ImageTemplateImportRequest request) {
        String labelType = normalizeRequiredLabelType(trimToDefault(request.getLabelType(), "BOX"));
        String name = trimToDefault(request.getSampleName(), "image-sample");
        LabelTemplate template = LabelTemplate.configLayout(uniqueCode("image-" + name.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase()), name, labelType, 120, 80);
        template.setImportSource("IMAGE");
        template.setElements(normalizeElements(imageDraftElements(name)));
        templateRepository.save(template);
        templateRepository.replaceElements(template.getCode(), template.getElements());
        templateRepository.recordImport(
                UUID.randomUUID().toString(),
                "IMAGE",
                template.getCode(),
                labelType,
                null,
                null,
                request.getSampleName(),
                "DRAFT_CREATED",
                "Image sample imported as editable Java template draft."
        );
        return templateRepository.findByCode(template.getCode()).copy();
    }

    public BoxLabelDiagnostics diagnose(String productConfigId, BoxLabelPrintRow row) {
        LabelTemplate template = requireTemplate(row.getTemplateCode());
        BoxLabelDiagnostics diagnostics = new BoxLabelDiagnostics();
        diagnostics.setProductConfigId(productConfigId);
        diagnostics.setTemplateAvailable(isTemplateAvailable(template));
        diagnostics.setLogoAvailable(row.getLogoPath() == null || row.getLogoPath().trim().isEmpty() || Files.exists(Paths.get(row.getLogoPath())));
        diagnostics.setBarcodeValid(row.getProductBarcode() != null && row.getProductBarcode().matches("[0-9A-Za-z\\-]{6,40}"));
        requireField(diagnostics, "boxLabelName", row.getBoxLabelName());
        requireField(diagnostics, "packageSpec", row.getPackageSpec());
        requireField(diagnostics, "productBarcode", row.getProductBarcode());
        requireField(diagnostics, "company", row.getCompany());
        requireField(diagnostics, "address", row.getAddress());
        requireField(diagnostics, "productionLicenseNo", row.getProductionLicenseNo());
        requireField(diagnostics, "productStandardNo", row.getProductStandardNo());
        if (!diagnostics.isTemplateAvailable()) {
            diagnostics.getWarnings().add("Template is missing or has no layout elements.");
        }
        if (!diagnostics.isLogoAvailable()) {
            diagnostics.getWarnings().add("Logo path is configured but the file is missing.");
        }
        if (!diagnostics.isBarcodeValid()) {
            diagnostics.getWarnings().add("Product barcode is blank or outside the accepted Code128 payload range.");
        }
        return diagnostics;
    }

    private boolean isTemplateAvailable(LabelTemplate template) {
        if ("CONFIG_LAYOUT".equals(template.getEngine())) {
            return template.getElements() != null && !template.getElements().isEmpty();
        }
        return template.getFileName() != null && Files.exists(Paths.get(properties.getTemplatePath(), template.getFileName()));
    }

    private LabelTemplate requireMutableTemplate(String templateCode) {
        LabelTemplate template = templateRepository.findByCode(requireCode(templateCode));
        if (template == null) {
            throw new IllegalArgumentException("Unknown label template: " + templateCode);
        }
        if (!"CONFIG_LAYOUT".equals(template.getEngine())) {
            throw new IllegalArgumentException("Only CONFIG_LAYOUT templates can be edited: " + templateCode);
        }
        return template;
    }

    private void requireField(BoxLabelDiagnostics diagnostics, String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            diagnostics.getMissingRequiredFields().add(fieldName);
        }
    }

    private List<LabelTemplateElement> defaultElements(String labelType, String title) {
        List<LabelTemplateElement> elements = new ArrayList<LabelTemplateElement>();
        elements.add(element("STATIC_TEXT", title, null, 4, 4, 70, 8, 14, true));
        elements.add(element("FIELD_TEXT", null, "boxLabelName", 4, 14, 90, 8, 12, false));
        elements.add(element("FIELD_TEXT", null, "packageSpec", 4, 24, 70, 7, 10, false));
        elements.add(element("BARCODE", null, null, 4, "BAG".equals(labelType) ? 34 : 56, 65, 16, 10, false));
        elements.add(element("QRCODE", null, null, 92, "BAG".equals(labelType) ? 24 : 48, 22, 22, 10, false));
        return elements;
    }

    private List<LabelTemplateElement> imageDraftElements(String sampleName) {
        List<LabelTemplateElement> elements = defaultElements("BOX", sampleName);
        elements.add(element("RECTANGLE", null, null, 2, 2, 116, 76, 10, false));
        return elements;
    }

    private LabelTemplateElement element(String type, String text, String fieldName, double left, double top, double width, double height, int fontSize, boolean bold) {
        LabelTemplateElement element = new LabelTemplateElement();
        element.setId(UUID.randomUUID().toString());
        element.setType(type);
        element.setText(text);
        element.setFieldName(fieldName);
        element.setLeftMm(left);
        element.setTopMm(top);
        element.setWidthMm(width);
        element.setHeightMm(height);
        element.setFontSize(fontSize);
        element.setBold(bold);
        return element;
    }

    private LabelTemplateElement copyElement(LabelTemplateElement element) {
        LabelTemplateElement copy = new LabelTemplateElement();
        copy.setId(element.getId());
        copy.setSortOrder(element.getSortOrder());
        copy.setType(trimToDefault(element.getType(), "STATIC_TEXT").toUpperCase());
        copy.setText(element.getText());
        copy.setFieldName(element.getFieldName());
        copy.setLeftMm(element.getLeftMm());
        copy.setTopMm(element.getTopMm());
        copy.setWidthMm(positiveOrDefault(element.getWidthMm(), 20));
        copy.setHeightMm(positiveOrDefault(element.getHeightMm(), 6));
        copy.setFontSize(element.getFontSize() <= 0 ? 10 : element.getFontSize());
        copy.setBold(element.isBold());
        return copy;
    }

    private List<LabelTemplateElement> normalizeElements(List<LabelTemplateElement> source) {
        List<LabelTemplateElement> elements = new ArrayList<LabelTemplateElement>();
        int index = 1;
        for (LabelTemplateElement element : source) {
            LabelTemplateElement copy = copyElement(element);
            if (copy.getId() == null || copy.getId().trim().isEmpty()) {
                copy.setId(UUID.randomUUID().toString());
            }
            if (copy.getSortOrder() <= 0) {
                copy.setSortOrder(index);
            }
            elements.add(copy);
            index++;
        }
        return elements;
    }

    private String uniqueCode(String base) {
        String code = requireCode(base);
        if (templateRepository.findByCode(code) == null) {
            return code;
        }
        int index = 2;
        while (templateRepository.findByCode(code + "-" + index) != null) {
            index++;
        }
        return code + "-" + index;
    }

    private String requireCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Template code is required.");
        }
        return value.trim();
    }

    private String normalizeOptionalLabelType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return normalizeRequiredLabelType(value);
    }

    private String normalizeRequiredLabelType(String value) {
        String normalized = trimToDefault(value, "BOX").toUpperCase();
        if (!"BOX".equals(normalized) && !"BAG".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported labelType: " + value);
        }
        return normalized;
    }

    private double positiveOrDefault(double value, double defaultValue) {
        return value > 0 ? value : defaultValue;
    }

    private String trimToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private int parseVersion(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 1;
        }
    }
}
