package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.BoxLabelApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BoxLabelApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = LabelPrintingPersistenceTest.OutputInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LabelPrintingPersistenceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void flywayCreatesJavaTablesAndSeedsDefaultTemplatesAndBinding() throws Exception {
        assertThat(tableCount("LP_LABEL_TEMPLATE")).isGreaterThanOrEqualTo(2);
        assertThat(tableCount("LP_LABEL_TEMPLATE_ELEMENT")).isGreaterThanOrEqualTo(5);
        assertThat(tableCount("LP_PRODUCT_TEMPLATE_BINDING")).isGreaterThanOrEqualTo(1);
        assertThat(tableCount("LP_PRINT_JOB")).isZero();
        assertThat(tableCount("LP_TEMPLATE_IMPORT_LOG")).isZero();

        mockMvc.perform(get("/api/product-template-bindings/DEMO-BOX-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxTemplateCode").value("box-standard"))
                .andExpect(jsonPath("$.bagTemplateCode").value("bag-standard"))
                .andExpect(jsonPath("$.source").value("JAVA_BINDING"));
    }

    @Test
    void templateElementsBindingsPrintJobsAndImportLogsArePersisted() throws Exception {
        mockMvc.perform(post("/api/label-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"box-db-custom\",\"name\":\"DB Custom Box\",\"labelType\":\"BOX\",\"pageWidthMm\":120,\"pageHeightMm\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("box-db-custom"))
                .andExpect(jsonPath("$.version").value("1"));

        mockMvc.perform(put("/api/label-templates/box-db-custom/elements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"elements\":["
                                + "{\"type\":\"STATIC_TEXT\",\"text\":\"DB Custom Box\",\"leftMm\":4,\"topMm\":4,\"widthMm\":70,\"heightMm\":8,\"fontSize\":14,\"bold\":true,\"textAlign\":\"center\",\"locked\":true,"
                                + "\"visibleWhenField\":\"brandName\",\"visibleWhenOperator\":\"equals\",\"visibleWhenValue\":\"QINRE\"},"
                                + "{\"type\":\"FIELD_TEXT\",\"fieldName\":\"boxLabelName\",\"leftMm\":4,\"topMm\":14,\"widthMm\":90,\"heightMm\":8,\"fontSize\":12,\"textAlign\":\"right\"},"
                                + "{\"type\":\"FIELD_TEXT\",\"fieldName\":\"packageSpec\",\"leftMm\":4,\"topMm\":24,\"widthMm\":90,\"heightMm\":7,\"fontSize\":10},"
                                + "{\"type\":\"BARCODE\",\"leftMm\":4,\"topMm\":56,\"widthMm\":65,\"heightMm\":16},"
                                + "{\"type\":\"QRCODE\",\"leftMm\":92,\"topMm\":48,\"widthMm\":22,\"heightMm\":22}"
                                + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("2"))
                .andExpect(jsonPath("$.elements.length()").value(5))
                .andExpect(jsonPath("$.elements[0].locked").value(true))
                .andExpect(jsonPath("$.elements[0].visibleWhenField").value("brandName"))
                .andExpect(jsonPath("$.elements[0].visibleWhenOperator").value("equals"))
                .andExpect(jsonPath("$.elements[0].visibleWhenValue").value("QINRE"));

        mockMvc.perform(put("/api/product-template-bindings/DEMO-BOX-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boxTemplateCode\":\"box-db-custom\",\"bagTemplateCode\":\"bag-standard\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxTemplateCode").value("box-db-custom"));

        mockMvc.perform(post("/api/box-labels/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"format\":\"PDF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("box-db-custom"))
                .andExpect(jsonPath("$.previewUrl", startsWith("/api/box-labels/files/")));

        mockMvc.perform(post("/api/box-labels/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productConfigId\":\"DEMO-BOX-001\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"format\":\"PDF\",\"printerName\":\"Browser\",\"copies\":2,\"operator\":\"tester\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BROWSER_PRINT_READY"));

        mockMvc.perform(post("/api/template-imports/from-legacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelType\":\"BOX\",\"legacyTemplateId\":\"LEGACY-001\",\"legacyTemplateName\":\"Legacy 001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importSource").value("LEGACY"));

        mockMvc.perform(post("/api/template-imports/from-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelType\":\"BOX\",\"sampleName\":\"sample-001.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importSource").value("IMAGE"));

        Map<String, Object> template = jdbcTemplate.queryForMap(
                "SELECT template_code, version_no, engine, label_type FROM LP_LABEL_TEMPLATE WHERE template_code = ?",
                "box-db-custom"
        );
        assertThat(template.get("template_code")).isEqualTo("box-db-custom");
        assertThat(String.valueOf(template.get("version_no"))).isEqualTo("2");
        assertThat(template.get("engine")).isEqualTo("CONFIG_LAYOUT");
        assertThat(template.get("label_type")).isEqualTo("BOX");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ?",
                Integer.class,
                "box-db-custom"
        )).isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT text_align FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ? AND sort_order = 1",
                String.class,
                "box-db-custom"
        )).isEqualTo("center");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT text_align FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ? AND sort_order = 2",
                String.class,
                "box-db-custom"
        )).isEqualTo("right");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT element_locked FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ? AND sort_order = 1",
                Boolean.class,
                "box-db-custom"
        )).isTrue();
        Map<String, Object> condition = jdbcTemplate.queryForMap(
                "SELECT visible_when_field, visible_when_operator, visible_when_value " +
                        "FROM LP_LABEL_TEMPLATE_ELEMENT WHERE template_code = ? AND sort_order = 1",
                "box-db-custom"
        );
        assertThat(condition.get("visible_when_field")).isEqualTo("brandName");
        assertThat(condition.get("visible_when_operator")).isEqualTo("equals");
        assertThat(condition.get("visible_when_value")).isEqualTo("QINRE");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT box_template_code FROM LP_PRODUCT_TEMPLATE_BINDING WHERE product_config_id = ?",
                String.class,
                "DEMO-BOX-001"
        )).isEqualTo("box-db-custom");
        assertThat(tableCount("LP_PRINT_JOB")).isEqualTo(1);
        assertThat(tableCount("LP_TEMPLATE_IMPORT_LOG")).isEqualTo(2);
    }

    private int tableCount(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }
}
