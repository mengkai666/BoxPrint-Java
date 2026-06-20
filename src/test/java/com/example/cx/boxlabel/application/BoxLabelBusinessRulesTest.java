package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BoxLabelBusinessRulesTest {

    private final BoxLabelBusinessRules rules = new BoxLabelBusinessRules();

    @Test
    void preparesLegacyTitleSpacingForShortChineseNames() {
        BoxLabelPrintRow row = sampleRow();
        row.setBoxLabelName("牛肠串");

        BoxLabelPrintRow prepared = rules.prepare(row, LocalDate.of(2026, 6, 8), "A");

        assertThat(prepared.getBoxLabelName()).isEqualTo("牛 肠 串");
    }

    @Test
    void convertsShelfLifeToExpiryDateWhenRequested() {
        BoxLabelPrintRow row = sampleRow();
        row.setShelfLife("12个月");
        row.setShelfLifeConvert(true);

        BoxLabelPrintRow prepared = rules.prepare(row, LocalDate.of(2026, 6, 8), "A");

        assertThat(prepared.getShelfLife()).isEqualTo("至2027年06月07日");
        assertThat(prepared.getProductionDate()).isEqualTo("2026年06月08日");
    }

    @Test
    void extractsWatermarkAndPrivateBrandFromMemo() {
        BoxLabelPrintRow watermark = sampleRow();
        watermark.setAddMemo("水印测试");

        BoxLabelPrintRow preparedWatermark = rules.prepare(watermark, LocalDate.of(2026, 6, 8), "A");

        assertThat(preparedWatermark.getWatermark()).isEqualTo("测试");
        assertThat(preparedWatermark.getAddMemo()).isEmpty();

        BoxLabelPrintRow privateBrand = sampleRow();
        privateBrand.setAddMemo("专供七十七路");

        BoxLabelPrintRow preparedPrivateBrand = rules.prepare(privateBrand, LocalDate.of(2026, 6, 8), "A");

        assertThat(preparedPrivateBrand.getBrandName()).isEqualTo("七十七路");
        assertThat(preparedPrivateBrand.getAddMemo()).isEqualTo("专供");
    }

    @Test
    void fallsBackToProductBarcodeAndCode128Type() {
        BoxLabelPrintRow row = sampleRow();
        row.setProductBarcode("");
        row.setOriginalProductBarcode("6900000000000");
        row.setOriginalCustomerBarcode("");
        row.setBarcodeType(null);

        BoxLabelPrintRow prepared = rules.prepare(row, LocalDate.of(2026, 6, 8), "A");

        assertThat(prepared.getProductBarcode()).isEqualTo("6900000000000");
        assertThat(prepared.getBarcodeType()).isEqualTo(151);
    }

    private BoxLabelPrintRow sampleRow() {
        BoxLabelPrintRow row = new BoxLabelPrintRow();
        row.setBoxTemplateName("箱贴通用模版");
        row.setBoxLabelName("示例产品");
        row.setInventoryName("示例产品");
        row.setProductBarcode("6900000000000");
        row.setShelfLife("12个月");
        row.setAddMemo("");
        return row;
    }
}
