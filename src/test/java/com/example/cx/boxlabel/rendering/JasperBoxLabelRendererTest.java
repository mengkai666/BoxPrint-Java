package com.example.cx.boxlabel.rendering;

import com.example.cx.boxlabel.config.PrintingProperties;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.LabelTemplateElement;
import com.example.cx.boxlabel.domain.RenderedLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JasperBoxLabelRendererTest {

    @TempDir
    Path tempDir;

    @Test
    void rendersPdfAndPngFromJasperTemplate() {
        PrintingProperties properties = new PrintingProperties();
        properties.setTemplatePath("src/main/resources/reports");
        properties.setOutputPath(tempDir.toString());
        JasperBoxLabelRenderer renderer = new JasperBoxLabelRenderer(
                new BarcodeImageService(),
                new FileSystemRenderOutputStore(properties)
        );

        LabelTemplate template = LabelTemplate.enabled("box-standard", "箱贴通用模板", "box-label-standard.jrxml", "1.0");

        RenderedLabel pdf = renderer.render(sampleRow(), template, LabelOutputFormat.PDF);
        RenderedLabel png = renderer.render(sampleRow(), template, LabelOutputFormat.PNG);

        assertThat(Files.exists(pdf.getFilePath())).isTrue();
        assertThat(Files.exists(png.getFilePath())).isTrue();
        assertThat(pdf.getSizeBytes()).isGreaterThan(1000);
        assertThat(png.getSizeBytes()).isGreaterThan(1000);
        assertThat(pdf.getPreviewUrl()).startsWith("/api/box-labels/files/");
    }

    @Test
    void rendersConfigLayoutWithTextAlignmentAndBoldStyles() {
        PrintingProperties properties = new PrintingProperties();
        properties.setTemplatePath("src/main/resources/reports");
        properties.setOutputPath(tempDir.toString());
        JasperBoxLabelRenderer renderer = new JasperBoxLabelRenderer(
                new BarcodeImageService(),
                new FileSystemRenderOutputStore(properties)
        );
        LabelTemplate template = LabelTemplate.configLayout("config-align", "Config Align", "BOX", 120, 80);
        template.setElements(java.util.Arrays.asList(
                element("STATIC_TEXT", "Centered title", null, 4, 4, 80, 8, 14, true, "center"),
                element("FIELD_TEXT", null, "boxLabelName", 4, 16, 90, 8, 12, true, "right"),
                element("BARCODE", null, null, 4, 56, 65, 16, 10, false, "left")
        ));

        RenderedLabel pdf = renderer.render(sampleRow(), template, LabelOutputFormat.PDF);
        RenderedLabel png = renderer.render(sampleRow(), template, LabelOutputFormat.PNG);

        assertThat(Files.exists(pdf.getFilePath())).isTrue();
        assertThat(Files.exists(png.getFilePath())).isTrue();
        assertThat(pdf.getSizeBytes()).isGreaterThan(1000);
        assertThat(png.getSizeBytes()).isGreaterThan(1000);
    }

    @Test
    void skipsConditionallyHiddenConfigElementsDuringRendering() {
        PrintingProperties properties = new PrintingProperties();
        properties.setTemplatePath("src/main/resources/reports");
        properties.setOutputPath(tempDir.toString());
        JasperBoxLabelRenderer renderer = new JasperBoxLabelRenderer(
                new BarcodeImageService(),
                new FileSystemRenderOutputStore(properties)
        );
        LabelTemplateElement hidden = element("FIELD_TEXT", null, "fieldThatDoesNotExist", 4, 4, 80, 8, 12, false, "left");
        hidden.setVisibleWhenField("brandName");
        hidden.setVisibleWhenOperator("equals");
        hidden.setVisibleWhenValue("MISSING_BRAND");
        LabelTemplate template = LabelTemplate.configLayout("config-conditional", "Config Conditional", "BOX", 120, 80);
        template.setElements(java.util.Arrays.asList(
                hidden,
                element("STATIC_TEXT", "Visible title", null, 4, 16, 80, 8, 14, true, "left")
        ));

        RenderedLabel png = renderer.render(sampleRow(), template, LabelOutputFormat.PNG);

        assertThat(Files.exists(png.getFilePath())).isTrue();
        assertThat(png.getSizeBytes()).isGreaterThan(1000);
    }

    private BoxLabelPrintRow sampleRow() {
        BoxLabelPrintRow row = new BoxLabelPrintRow();
        row.setBoxTemplateName("箱贴通用模版");
        row.setBoxLabelName("示 例 产 品 箱 贴");
        row.setInventoryName("示例产品");
        row.setPackageSpec("1kg*10袋/箱");
        row.setBrandName("示例品牌");
        row.setProductionDate("2026年06月08日");
        row.setShift("A");
        row.setStorageCondition("-18℃以下冷冻保存");
        row.setShelfLife("12个月");
        row.setProductBarcode("6900000000000");
        row.setQrCode("FA4C5A54-0000-0000-0000-326133653233");
        row.setCompany("示例生产公司");
        row.setAddress("示例生产地址");
        row.setContact("400-000-0000");
        row.setProductionLicenseNo("SC00000000000000");
        row.setProductStandardNo("Q/DEMO 0001S");
        row.setIngredients("配料表：示例配料");
        row.setNutritionFacts("营养成份：示例");
        row.setNetContent("净含量：1kg");
        return row;
    }

    private LabelTemplateElement element(String type,
                                         String text,
                                         String fieldName,
                                         double leftMm,
                                         double topMm,
                                         double widthMm,
                                         double heightMm,
                                         int fontSize,
                                         boolean bold,
                                         String textAlign) {
        LabelTemplateElement element = new LabelTemplateElement();
        element.setId(type + "-" + leftMm + "-" + topMm);
        element.setType(type);
        element.setText(text);
        element.setFieldName(fieldName);
        element.setLeftMm(leftMm);
        element.setTopMm(topMm);
        element.setWidthMm(widthMm);
        element.setHeightMm(heightMm);
        element.setFontSize(fontSize);
        element.setBold(bold);
        element.setTextAlign(textAlign);
        return element;
    }
}
