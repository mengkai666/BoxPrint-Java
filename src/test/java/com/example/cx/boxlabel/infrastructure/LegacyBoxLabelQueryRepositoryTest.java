package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.BoxLabelApplication;
import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BoxLabelApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LegacyBoxLabelQueryRepositoryTest {

    @Autowired
    private BoxLabelQueryRepository queryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void defaultProfileReadsProductSearchFromFakeLegacyTables() {
        assertThat(tableCount("hsh_Name_Conversion")).isGreaterThanOrEqualTo(2);
        assertThat(tableCount("Inventory")).isGreaterThanOrEqualTo(2);
        assertThat(tableCount("CD_PRINT_TEMPLATE")).isGreaterThanOrEqualTo(2);

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword("SQL Legacy Product");

        List<BoxLabelProductSummary> products = queryRepository.searchProducts(criteria);

        assertThat(products).hasSize(1);
        BoxLabelProductSummary product = products.get(0);
        assertThat(product.getProductConfigId()).isEqualTo("SQL-BOX-002");
        assertThat(product.getInventoryCode()).isEqualTo("SQL002");
        assertThat(product.getInventoryName()).isEqualTo("SQL Legacy Product");
        assertThat(product.getBrandName()).isEqualTo("SQL Legacy Brand");
        assertThat(product.getBoxTemplateCode()).isEqualTo("box-standard");
        assertThat(product.getBoxTemplateName()).isEqualTo("Legacy Box Template");
    }

    @Test
    void defaultProfileMapsFakeLegacyTablesToBoxLabelPrintRow() {
        BoxLabelPrintRequest request = new BoxLabelPrintRequest();
        request.setProductConfigId("SQL-BOX-002");
        request.setProductionDate("2026-06-19");
        request.setShift("B");

        BoxLabelPrintRow row = queryRepository.findBoxLabelRow(request);

        assertThat(row.getTemplateCode()).isEqualTo("box-standard");
        assertThat(row.getBoxTemplateName()).isEqualTo("Legacy Box Template");
        assertThat(row.getBagTemplateName()).isEqualTo("Legacy Bag Template");
        assertThat(row.getInventoryName()).isEqualTo("SQL Legacy Product");
        assertThat(row.getPackageSpec()).isEqualTo("2kg*6 bags/carton");
        assertThat(row.getBoxLabelName()).isEqualTo("SQL Legacy Product Box Label");
        assertThat(row.getBrandName()).isEqualTo("SQL Legacy Brand");
        assertThat(row.getStorageCondition()).isEqualTo("-18℃以下冷冻保存，运输过程保持冷链");
        assertThat(row.getShelfLife()).isEqualTo("18 months");
        assertThat(row.getProductBarcode()).isEqualTo("6950000000002");
        assertThat(row.getCompany()).isEqualTo("SQL Legacy Foods Co., Ltd.");
        assertThat(row.getAddress()).isEqualTo("No. 2 Legacy Road");
        assertThat(row.getProductionLicenseNo()).isEqualTo("SC12345678900002");
        assertThat(row.getProductStandardNo()).isEqualTo("Q/SQL 0002S");
        assertThat(row.getIngredients()).isEqualTo("Ingredients: sample flour, water");
        assertThat(row.getNutritionFacts()).isEqualTo("Nutrition facts: sample data");
        assertThat(row.getNetContent()).isEqualTo("Net content: 2kg");
        assertThat(row.getLogoPath()).isEqualTo("");
        assertThat(row.getLogoSize()).isEqualTo(2);
        assertThat(row.getShelfLifeConvert()).isFalse();
    }

    private int tableCount(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }
}
