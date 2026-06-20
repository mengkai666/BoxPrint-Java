package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.BoxLabelApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BoxLabelApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = TemplateStudioControllerTest.OutputInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TemplateStudioControllerTest {

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
    void nameConversionEndpointsExposeProductDetailAndJavaTemplateBinding() throws Exception {
        mockMvc.perform(get("/api/name-conversions/products").param("keyword", "DEMO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].productConfigId").value("DEMO-BOX-001"));

        mockMvc.perform(get("/api/name-conversions/DEMO-BOX-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productConfigId").value("DEMO-BOX-001"))
                .andExpect(jsonPath("$.templateBinding.boxTemplateCode").value("box-standard"))
                .andExpect(jsonPath("$.templateBinding.bagTemplateCode").value("bag-standard"))
                .andExpect(jsonPath("$.fieldSources.length()", greaterThan(25)))
                .andExpect(jsonPath("$.fieldSources[*].fieldName", hasItem("productBarcode")))
                .andExpect(jsonPath("$.fieldSources[*].fieldName", hasItem("qrCode")))
                .andExpect(jsonPath("$.fieldSources[*].fieldName", hasItem("ingredients")))
                .andExpect(jsonPath("$.fieldSources[*].fieldName", hasItem("nutritionFacts")))
                .andExpect(jsonPath("$.fieldSources[*].fieldName", hasItem("consignorName")))
                .andExpect(jsonPath("$.fieldSources[*].fieldName", hasItem("logoSize")))
                .andExpect(jsonPath("$.fieldSources[*].displayName", hasItem("配料表")))
                .andExpect(jsonPath("$.fieldSources[*].category", hasItem("条码与追溯")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='ingredients')].value", hasItem(containsString("小麦粉"))))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='nutritionFacts')].value", hasItem(containsString("能量"))))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='productBarcode')].value", hasItem("6900000000000")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='boxTemplateCode')].sourceType", hasItem("JAVA_BINDING")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='boxTemplateCode')].fieldStatus", hasItem("OK")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='inventoryName')].sourceType", hasItem("LEGACY_TABLE")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='productBarcode')].sourceType", hasItem("FALLBACK_RULE")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='productBarcode')].fieldStatus", hasItem("FALLBACK")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='logoPath')].sourceType", hasItem("FILE_RESOLVER")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='logoPath')].fieldStatus", hasItem("MISSING")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='logoSize')].sourceType", hasItem("TEMPLATE_DEFAULT")))
                .andExpect(jsonPath("$.fieldSources[?(@.fieldName=='logoSize')].fieldStatus", hasItem("FALLBACK")));
    }

    @Test
    void templateMaintenanceCreatesConfigLayoutTemplateAndElements() throws Exception {
        mockMvc.perform(post("/api/label-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"box-custom\",\"name\":\"Custom Box\",\"labelType\":\"BOX\",\"pageWidthMm\":120,\"pageHeightMm\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("box-custom"))
                .andExpect(jsonPath("$.labelType").value("BOX"))
                .andExpect(jsonPath("$.engine").value("CONFIG_LAYOUT"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.version").value("1"));

        mockMvc.perform(put("/api/label-templates/box-custom/elements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"elements\":["
                                + "{\"type\":\"STATIC_TEXT\",\"text\":\"Custom Box Label\",\"leftMm\":4,\"topMm\":4,\"widthMm\":70,\"heightMm\":8,\"fontSize\":14},"
                                + "{\"type\":\"FIELD_TEXT\",\"fieldName\":\"boxLabelName\",\"leftMm\":4,\"topMm\":14,\"widthMm\":90,\"heightMm\":8,\"fontSize\":12},"
                                + "{\"type\":\"BARCODE\",\"leftMm\":4,\"topMm\":56,\"widthMm\":65,\"heightMm\":16},"
                                + "{\"type\":\"QRCODE\",\"leftMm\":92,\"topMm\":48,\"widthMm\":22,\"heightMm\":22}"
                                + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("box-custom"))
                .andExpect(jsonPath("$.version").value("2"))
                .andExpect(jsonPath("$.elements.length()").value(4));

        mockMvc.perform(get("/api/label-templates").param("labelType", "BOX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", greaterThan(1)));
    }

    @Test
    void productTemplateBindingDrivesPreviewAndRenderWhenRequestDoesNotSpecifyTemplate() throws Exception {
        createCustomBoxTemplate();

        mockMvc.perform(put("/api/product-template-bindings/DEMO-BOX-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boxTemplateCode\":\"box-custom\",\"bagTemplateCode\":\"bag-standard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxTemplateCode").value("box-custom"))
                .andExpect(jsonPath("$.bagTemplateCode").value("bag-standard"));

        mockMvc.perform(post("/api/box-labels/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("box-custom"))
                .andExpect(jsonPath("$.row.templateCode").value("box-custom"));

        mockMvc.perform(post("/api/box-labels/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"format\":\"PDF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("box-custom"))
                .andExpect(jsonPath("$.previewUrl", startsWith("/api/box-labels/files/")));
    }

    @Test
    void phaseOneIncludesEditableBoxConfigTemplateThatCanDrivePrintLoop() throws Exception {
        String templatesJson = mockMvc.perform(get("/api/label-templates").param("labelType", "BOX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.code=='box-config-standard')].engine").value(hasItem("CONFIG_LAYOUT")))
                .andExpect(jsonPath("$.items[?(@.code=='box-config-standard')].status").value(hasItem("ACTIVE")))
                .andExpect(jsonPath("$.items[?(@.code=='box-config-standard')].elements[0].length()").value(hasItem(greaterThan(8))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(templatesJson)
                .contains("\"code\":\"box-config-standard\"")
                .contains("\"engine\":\"CONFIG_LAYOUT\"")
                .contains("\"fieldName\":\"boxLabelName\"")
                .contains("\"type\":\"BARCODE\"")
                .contains("\"type\":\"QRCODE\"");

        mockMvc.perform(put("/api/product-template-bindings/DEMO-BOX-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boxTemplateCode\":\"box-config-standard\",\"bagTemplateCode\":\"bag-standard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxTemplateCode").value("box-config-standard"));

        mockMvc.perform(post("/api/box-labels/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"labelType\":\"BOX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("box-config-standard"))
                .andExpect(jsonPath("$.row.templateCode").value("box-config-standard"));

        mockMvc.perform(post("/api/box-labels/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"labelType\":\"BOX\",\"format\":\"PNG\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("box-config-standard"))
                .andExpect(jsonPath("$.format").value("PNG"))
                .andExpect(jsonPath("$.previewUrl", startsWith("/api/box-labels/files/")));

        mockMvc.perform(post("/api/box-labels/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"labelType\":\"BOX\",\"format\":\"PDF\",\"printerName\":\"Browser\",\"copies\":1,\"operator\":\"phase-one\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BROWSER_PRINT_READY"))
                .andExpect(jsonPath("$.output.templateCode").value("box-config-standard"));

        mockMvc.perform(get("/api/box-labels/print-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].output.templateCode").value("box-config-standard"));
    }

    @Test
    void nameConversionProductSearchReflectsJavaTemplateBindingAndTemplateFilter() throws Exception {
        createCustomBoxTemplate();

        mockMvc.perform(put("/api/product-template-bindings/DEMO-BOX-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boxTemplateCode\":\"box-custom\",\"bagTemplateCode\":\"bag-standard\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/name-conversions/products").param("keyword", "DEMO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productConfigId").value("DEMO-BOX-001"))
                .andExpect(jsonPath("$.items[0].boxTemplateCode").value("box-custom"))
                .andExpect(jsonPath("$.items[0].boxTemplateName").value("Custom Box"));

        mockMvc.perform(get("/api/name-conversions/products")
                        .param("keyword", "DEMO")
                        .param("templateCode", "box-custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productConfigId").value("DEMO-BOX-001"));

        mockMvc.perform(get("/api/name-conversions/products")
                        .param("keyword", "DEMO")
                        .param("templateCode", "box-standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void legacyAndImageImportsCreateEditableDraftTemplates() throws Exception {
        mockMvc.perform(post("/api/template-imports/from-legacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelType\":\"BOX\",\"legacyTemplateId\":\"00000\",\"legacyTemplateName\":\"Legacy Box Template\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.importSource").value("LEGACY"))
                .andExpect(jsonPath("$.elements.length()", greaterThan(0)));

        mockMvc.perform(post("/api/template-imports/from-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelType\":\"BOX\",\"sampleName\":\"box-sample.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.importSource").value("IMAGE"))
                .andExpect(jsonPath("$.elements.length()", greaterThan(0)));
    }

    @Test
    void printJobsAreQueryableAfterBrowserPrint() throws Exception {
        mockMvc.perform(post("/api/box-labels/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"format\":\"PDF\",\"printerName\":\"Browser\",\"copies\":1,\"operator\":\"tester\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/box-labels/print-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].status").value("BROWSER_PRINT_READY"))
                .andExpect(jsonPath("$.items[0].productConfigId").value("DEMO-BOX-001"))
                .andExpect(jsonPath("$.items[0].labelType").value("BOX"))
                .andExpect(jsonPath("$.items[0].operator").value("tester"))
                .andExpect(jsonPath("$.items[0].printerName").value("Browser"))
                .andExpect(jsonPath("$.items[0].copies").value(1))
                .andExpect(jsonPath("$.items[0].output.templateCode").value("box-standard"))
                .andExpect(jsonPath("$.items[0].output.fileId").exists())
                .andExpect(jsonPath("$.items[0].output.previewUrl", startsWith("/api/box-labels/files/")));
    }

    private void createCustomBoxTemplate() throws Exception {
        mockMvc.perform(post("/api/label-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"box-custom\",\"name\":\"Custom Box\",\"labelType\":\"BOX\",\"pageWidthMm\":120,\"pageHeightMm\":80}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/label-templates/box-custom/elements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"elements\":["
                                + "{\"type\":\"STATIC_TEXT\",\"text\":\"Custom Box Label\",\"leftMm\":4,\"topMm\":4,\"widthMm\":70,\"heightMm\":8,\"fontSize\":14},"
                                + "{\"type\":\"FIELD_TEXT\",\"fieldName\":\"boxLabelName\",\"leftMm\":4,\"topMm\":14,\"widthMm\":90,\"heightMm\":8,\"fontSize\":12},"
                                + "{\"type\":\"BARCODE\",\"leftMm\":4,\"topMm\":56,\"widthMm\":65,\"heightMm\":16},"
                                + "{\"type\":\"QRCODE\",\"leftMm\":92,\"topMm\":48,\"widthMm\":22,\"heightMm\":22}"
                                + "]}"))
                .andExpect(status().isOk());
    }
}
