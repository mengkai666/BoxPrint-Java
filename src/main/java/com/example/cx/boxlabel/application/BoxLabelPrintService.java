package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.BoxLabelDiagnostics;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobResponse;
import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintResponse;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.BoxLabelRenderRequest;
import com.example.cx.boxlabel.domain.BoxLabelRenderResponse;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;
import com.example.cx.boxlabel.domain.RenderedLabel;
import com.example.cx.boxlabel.domain.SearchResult;
import com.example.cx.boxlabel.infrastructure.BoxLabelQueryRepository;
import com.example.cx.boxlabel.infrastructure.PrintJobRepository;
import com.example.cx.boxlabel.rendering.BoxLabelRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BoxLabelPrintService {

    private static final String MODE_PURE_JAVA_PREVIEW = "PURE_JAVA_PREVIEW";

    private final BoxLabelQueryRepository queryRepository;
    private final BoxLabelBusinessRules businessRules;
    private final BoxLabelTemplateService templateService;
    private final ProductTemplateBindingService bindingService;
    private final BoxLabelRenderer renderer;
    private final PrintJobRepository printJobRepository;

    public BoxLabelPrintService(BoxLabelQueryRepository queryRepository,
                                BoxLabelBusinessRules businessRules,
                                BoxLabelTemplateService templateService,
                                ProductTemplateBindingService bindingService,
                                BoxLabelRenderer renderer,
                                PrintJobRepository printJobRepository) {
        this.queryRepository = queryRepository;
        this.businessRules = businessRules;
        this.templateService = templateService;
        this.bindingService = bindingService;
        this.renderer = renderer;
        this.printJobRepository = printJobRepository;
    }

    @Transactional(readOnly = true)
    public SearchResult<BoxLabelProductSummary> searchProducts(ProductSearchCriteria criteria) {
        return new SearchResult<BoxLabelProductSummary>(queryRepository.searchProducts(criteria));
    }

    @Transactional(readOnly = true)
    public BoxLabelPrintResponse preview(BoxLabelPrintRequest request) {
        BoxLabelPrintRow row = prepareRow(request);
        LabelTemplate template = templateService.requireTemplate(row.getTemplateCode());
        return BoxLabelPrintResponse.success(MODE_PURE_JAVA_PREVIEW, "Box label data prepared by pure Java service.", template, row);
    }

    @Transactional(readOnly = true)
    public BoxLabelRenderResponse render(BoxLabelRenderRequest request) {
        BoxLabelPrintRow row = prepareRow(request);
        LabelTemplate template = templateService.requireTemplate(row.getTemplateCode());
        RenderedLabel rendered = renderer.render(row, template, LabelOutputFormat.from(request.getFormat()));
        return BoxLabelRenderResponse.from(rendered);
    }

    @Transactional
    public BoxLabelPrintJobResponse print(BoxLabelPrintJobRequest request) {
        BoxLabelPrintRow row = prepareRow(request);
        LabelTemplate template = templateService.requireTemplate(row.getTemplateCode());
        RenderedLabel rendered = renderer.render(row, template, LabelOutputFormat.from(request.getFormat()));
        return printJobRepository.recordBrowserPrint(request, rendered);
    }

    @Transactional(readOnly = true)
    public SearchResult<LabelTemplate> listTemplates() {
        return new SearchResult<LabelTemplate>(templateService.listTemplates());
    }

    @Transactional(readOnly = true)
    public SearchResult<BoxLabelPrintJobResponse> listPrintJobs() {
        return new SearchResult<BoxLabelPrintJobResponse>(printJobRepository.listRecent());
    }

    @Transactional(readOnly = true)
    public BoxLabelDiagnostics diagnose(String productConfigId) {
        BoxLabelPrintRequest request = new BoxLabelPrintRequest();
        request.setProductConfigId(productConfigId);
        BoxLabelPrintRow row = prepareRow(request);
        return templateService.diagnose(productConfigId, row);
    }

    private BoxLabelPrintRow prepareRow(BoxLabelPrintRequest request) {
        BoxLabelPrintRow row = queryRepository.findBoxLabelRow(request);
        String templateCode = bindingService.resolveTemplateCode(
                request.getProductConfigId(),
                request.getLabelType(),
                request.getTemplateCode()
        );
        row.setTemplateCode(templateCode);
        return businessRules.prepare(row, parseProductionDate(request.getProductionDate()), request.getShift());
    }

    private LocalDate parseProductionDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(value.trim());
    }
}
