package com.example.cx.boxlabel.rendering;

import com.example.cx.boxlabel.config.PrintingProperties;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.LabelTemplate;
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
}
