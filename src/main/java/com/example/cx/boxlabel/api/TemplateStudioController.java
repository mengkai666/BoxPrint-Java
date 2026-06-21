package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.application.BoxLabelPrintService;
import com.example.cx.boxlabel.application.BoxLabelTemplateService;
import com.example.cx.boxlabel.domain.BoxLabelRenderRequest;
import com.example.cx.boxlabel.domain.BoxLabelRenderResponse;
import com.example.cx.boxlabel.domain.ImageTemplateImportRequest;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.LabelTemplateCopyRequest;
import com.example.cx.boxlabel.domain.LabelTemplateElementsUpdateRequest;
import com.example.cx.boxlabel.domain.LabelTemplateSaveRequest;
import com.example.cx.boxlabel.domain.LegacyTemplateImportRequest;
import com.example.cx.boxlabel.domain.SearchResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
public class TemplateStudioController {

    private final BoxLabelTemplateService templateService;
    private final BoxLabelPrintService printService;

    public TemplateStudioController(BoxLabelTemplateService templateService,
                                    BoxLabelPrintService printService) {
        this.templateService = templateService;
        this.printService = printService;
    }

    @GetMapping("/api/label-templates")
    public SearchResult<LabelTemplate> templates(@RequestParam(required = false) String labelType) {
        return new SearchResult<LabelTemplate>(templateService.listTemplates(labelType));
    }

    @PostMapping("/api/label-templates")
    public LabelTemplate saveTemplate(@Valid @RequestBody LabelTemplateSaveRequest request) {
        return templateService.saveTemplate(request);
    }

    @PutMapping("/api/label-templates/{templateCode}/elements")
    public LabelTemplate updateElements(@PathVariable String templateCode,
                                        @RequestBody LabelTemplateElementsUpdateRequest request) {
        return templateService.updateElements(templateCode, request);
    }

    @PostMapping("/api/label-templates/{templateCode}/copy")
    public LabelTemplate copyTemplate(@PathVariable String templateCode,
                                      @RequestBody LabelTemplateCopyRequest request) {
        return templateService.copyTemplate(templateCode, request);
    }

    @PostMapping("/api/label-templates/{templateCode}/preview")
    public BoxLabelRenderResponse previewTemplate(@PathVariable String templateCode,
                                                  @RequestBody(required = false) BoxLabelRenderRequest request) {
        BoxLabelRenderRequest safeRequest = request == null ? new BoxLabelRenderRequest() : request;
        if (safeRequest.getProductConfigId() == null || safeRequest.getProductConfigId().trim().isEmpty()) {
            safeRequest.setProductConfigId("DEMO-BOX-001");
        }
        safeRequest.setTemplateCode(templateCode);
        if (safeRequest.getFormat() == null || safeRequest.getFormat().trim().isEmpty()) {
            safeRequest.setFormat("PDF");
        }
        return printService.render(safeRequest);
    }

    @PostMapping("/api/template-imports/from-legacy")
    public LabelTemplate importFromLegacy(@RequestBody LegacyTemplateImportRequest request) {
        return templateService.importFromLegacy(request);
    }

    @PostMapping("/api/template-imports/from-image")
    public LabelTemplate importFromImage(@RequestBody ImageTemplateImportRequest request) {
        return templateService.importFromImage(request);
    }
}
