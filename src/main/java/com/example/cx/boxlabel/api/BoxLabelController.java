package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.application.BoxLabelPrintService;
import com.example.cx.boxlabel.domain.BoxLabelDiagnostics;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobResponse;
import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintResponse;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.BoxLabelRenderRequest;
import com.example.cx.boxlabel.domain.BoxLabelRenderResponse;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;
import com.example.cx.boxlabel.domain.SearchResult;
import com.example.cx.boxlabel.rendering.FileSystemRenderOutputStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.nio.file.Path;

@Validated
@RestController
@RequestMapping("/api/box-labels")
public class BoxLabelController {

    private final BoxLabelPrintService boxLabelPrintService;
    private final FileSystemRenderOutputStore outputStore;

    public BoxLabelController(BoxLabelPrintService boxLabelPrintService,
                              FileSystemRenderOutputStore outputStore) {
        this.boxLabelPrintService = boxLabelPrintService;
        this.outputStore = outputStore;
    }

    @GetMapping("/products")
    public SearchResult<BoxLabelProductSummary> products(ProductSearchCriteria criteria) {
        return boxLabelPrintService.searchProducts(criteria);
    }

    @PostMapping("/preview")
    public BoxLabelPrintResponse preview(@Valid @RequestBody BoxLabelPrintRequest request) {
        return boxLabelPrintService.preview(request);
    }

    @PostMapping("/render")
    public BoxLabelRenderResponse render(@Valid @RequestBody BoxLabelRenderRequest request) {
        return boxLabelPrintService.render(request);
    }

    @PostMapping("/print")
    public BoxLabelPrintJobResponse print(@Valid @RequestBody BoxLabelPrintJobRequest request) {
        return boxLabelPrintService.print(request);
    }

    @GetMapping("/templates")
    public SearchResult<LabelTemplate> templates() {
        return boxLabelPrintService.listTemplates();
    }

    @GetMapping("/print-jobs")
    public SearchResult<BoxLabelPrintJobResponse> printJobs() {
        return boxLabelPrintService.listPrintJobs();
    }

    @GetMapping("/diagnostics/{productConfigId}")
    public BoxLabelDiagnostics diagnostics(@PathVariable String productConfigId) {
        return boxLabelPrintService.diagnose(productConfigId);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> file(@PathVariable String fileId) {
        Path file = outputStore.findFile(fileId);
        MediaType mediaType = file.getFileName().toString().endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.APPLICATION_PDF;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(file.toFile()));
    }
}
