package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.BoxLabelApplication;
import com.example.cx.boxlabel.config.PrintingProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BoxLabelApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = BoxLabelControllerTest.OutputInitializer.class)
class BoxLabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    static class OutputInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "cx.printing.output-path=target/test-boxlabel-output",
                    "cx.printing.template-path=src/main/resources/reports",
                    "cx.printing.logo-path=src/test/resources/logo"
            ).applyTo(context.getEnvironment());
        }
    }

    @Test
    void productsEndpointReturnsSearchableBoxLabelProducts() throws Exception {
        mockMvc.perform(get("/api/box-labels/products").param("keyword", "示例"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].productConfigId").value("DEMO-BOX-001"))
                .andExpect(jsonPath("$.items[0].boxTemplateCode").value("box-standard"));
    }

    @Test
    void previewReturnsPureJavaRowAndMetadata() throws Exception {
        mockMvc.perform(post("/api/box-labels/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"templateCode\":\"box-standard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.mode").value("PURE_JAVA_PREVIEW"))
                .andExpect(jsonPath("$.templateCode").value("box-standard"))
                .andExpect(jsonPath("$.row.boxLabelName").value("示 例 产 品 箱 贴"))
                .andExpect(jsonPath("$.row.productBarcode").value("6900000000000"));
    }

    @Test
    void renderEndpointCreatesPreviewFileWithoutVfpCompatibility() throws Exception {
        mockMvc.perform(post("/api/box-labels/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"templateCode\":\"box-standard\",\"format\":\"PDF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.fileId", not("")))
                .andExpect(jsonPath("$.previewUrl", startsWith("/api/box-labels/files/")))
                .andExpect(jsonPath("$.templateVersion").value("1.0"));
    }

    @Test
    void printEndpointRecordsBrowserPrintJob() throws Exception {
        mockMvc.perform(post("/api/box-labels/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"templateCode\":\"box-standard\",\"format\":\"PDF\",\"printerName\":\"浏览器默认打印机\",\"copies\":2,\"operator\":\"tester\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("BROWSER_PRINT_READY"))
                .andExpect(jsonPath("$.copies").value(2))
                .andExpect(jsonPath("$.output.previewUrl", startsWith("/api/box-labels/files/")));
    }

    @Test
    void templatesEndpointReturnsJasperTemplates() throws Exception {
        mockMvc.perform(get("/api/box-labels/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].code").value("box-standard"))
                .andExpect(jsonPath("$.items[0].engine").value("JASPER"))
                .andExpect(jsonPath("$.items[0].enabled").value(true));
    }

    @Test
    void diagnosticsEndpointReportsTemplateLogoAndFieldStatus() throws Exception {
        mockMvc.perform(get("/api/box-labels/diagnostics/DEMO-BOX-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productConfigId").value("DEMO-BOX-001"))
                .andExpect(jsonPath("$.templateAvailable").value(true))
                .andExpect(jsonPath("$.barcodeValid").value(true))
                .andExpect(jsonPath("$.missingRequiredFields.length()").value(0));
    }

    @Test
    void rootAndPrintWorkbenchServePrintOnlyWorkspace() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("print-workbench.html"));

        mockMvc.perform(get("/print-workbench"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("print-workbench.html"));

        String html = new String(
                mockMvc.perform(get("/print-workbench.html"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray(),
                StandardCharsets.UTF_8
        );
        org.assertj.core.api.Assertions.assertThat(html)
                .contains("箱贴打印工作台")
                .contains("function renderPreviewGroups")
                .contains("diagnosticSummary")
                .contains("renderDiagnostics")
                .contains("field-source-groups")
                .contains("field-source-group")
                .contains("details open")
                .contains("function shouldExpandFieldGroup")
                .contains("function renderTemplateFilterOptions")
                .contains("function requireSelectedProduct")
                .contains("function runAction")
                .contains("function handleActionError")
                .contains("class=\"output-link\"")
                .contains("$('labelType').onchange = () => runAction('leftStatus', async () => {")
                .contains("state.templates")
                .contains("state.binding")
                .contains("function syncTemplateFromBinding")
                .contains("function groupFieldSources")
                .contains("function renderFieldSources")
                .contains("source-category")
                .contains("source-display-name")
                .contains("source-field-name")
                .contains("source-status")
                .contains("status-ok")
                .contains("status-missing")
                .contains("status-fallback")
                .contains("job-template")
                .contains("item.productConfigId")
                .contains("item.operator")
                .contains("item.output.templateCode")
                .doesNotContain("templateCanvas")
                .doesNotContain("fieldPalette")
                .doesNotContain("elementInspector")
                .doesNotContain("function renderTemplateEditor")
                .doesNotContain("function addTemplateElement")
                .doesNotContain("function beginElementDrag")
                .doesNotContain("function saveEditorElements")
                .doesNotContain("function renderFieldPalette")
                .doesNotContain("旧模板导入")
                .doesNotContain("样张识别");
    }
}
