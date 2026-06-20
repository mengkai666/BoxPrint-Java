package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.BoxLabelFieldMapping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoxLabelFieldContractServiceTest {

    private final BoxLabelFieldContractService service = new BoxLabelFieldContractService();

    @Test
    void includesRequiredLegacyBoxLabelFields() {
        List<BoxLabelFieldMapping> mappings = service.listMappings();

        assertThat(mappings)
                .extracting(BoxLabelFieldMapping::getVfpFieldName)
                .contains("箱贴模版", "箱贴名称", "产品条码", "公司", "地址", "生产许可证号", "产品标准号");
    }

    @Test
    void mapsLogoFieldsWithoutMakingThemRequiredForEveryTemplate() {
        List<BoxLabelFieldMapping> mappings = service.listMappings();

        assertThat(mappings)
                .filteredOn(mapping -> "LOGO".equals(mapping.getVfpFieldName()))
                .singleElement()
                .satisfies(mapping -> {
                    assertThat(mapping.getJavaFieldName()).isEqualTo("logoPath");
                    assertThat(mapping.isRequiredForFrx()).isFalse();
                });
    }
}

