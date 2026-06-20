package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class SampleBoxLabelQueryRepository implements BoxLabelQueryRepository {

    @Override
    public List<BoxLabelProductSummary> searchProducts(ProductSearchCriteria criteria) {
        List<BoxLabelProductSummary> products = new ArrayList<BoxLabelProductSummary>();
        BoxLabelProductSummary product = new BoxLabelProductSummary();
        product.setProductConfigId("DEMO-BOX-001");
        product.setInventoryCode("DEMO001");
        product.setInventoryName("示例产品");
        product.setBrandName("示例品牌");
        product.setBoxTemplateCode("box-standard");
        product.setBoxTemplateName("箱贴通用模版");
        if (matches(product, criteria)) {
            products.add(product);
        }
        return products;
    }

    @Override
    public BoxLabelPrintRow findBoxLabelRow(BoxLabelPrintRequest request) {
        BoxLabelPrintRow row = new BoxLabelPrintRow();
        row.setTemplateCode(emptyToDefault(request.getTemplateCode(), "box-standard"));
        row.setBoxTemplateName("箱贴通用模版");
        row.setBagTemplateName("袋贴通用模版");
        row.setWatermark("");
        row.setShift(emptyToDefault(request.getShift(), "A"));
        row.setProductionDate(emptyToDefault(request.getProductionDate(), "2026-06-08"));
        row.setInventoryName("示例产品");
        row.setPackageSpec("1kg*10袋/箱");
        row.setBoxLabelName("示例产品箱贴");
        row.setBrandName("示例品牌");
        row.setAddMemo("");
        row.setStorageCondition("-18℃以下冷冻保存");
        row.setShelfLife("12个月");
        row.setWatermarkPlace(0);
        row.setQrCode("FA4C5A54-0000-0000-0000-326133653233");
        row.setProductBarcode("6900000000000");
        row.setOriginalProductBarcode("");
        row.setOriginalCustomerBarcode("");
        row.setConsignorName("示例委托商");
        row.setConsignorPhone("000-00000000");
        row.setConsignorAddress("示例地址");
        row.setConsignorType("委托商");
        row.setBarcodeType(151);
        row.setContact("400-000-0000");
        row.setCompany("示例生产公司");
        row.setAddress("示例生产地址");
        row.setCompanyTypeName("生产商");
        row.setOrigin("中国");
        row.setProductionLicenseNo("SC00000000000000");
        row.setProductStandardNo("Q/DEMO 0001S");
        row.setIngredients("配料表：示例配料");
        row.setNutritionFacts("营养成份：示例");
        row.setNetContent("净含量：1kg");
        row.setProductClass("速冻食品");
        row.setInstructions("充分加热后食用");
        row.setAllergenInfo("致敏原：无");
        row.setReminder("温馨提示：样例数据仅用于联调");
        row.setLongLogo(false);
        row.setLogoPath("");
        row.setLogoSize(2);
        row.setShelfLifeConvert(false);
        return row;
    }

    private boolean matches(BoxLabelProductSummary product, ProductSearchCriteria criteria) {
        if (criteria == null) {
            return true;
        }
        String keyword = trim(criteria.getKeyword());
        if (keyword != null && !product.getInventoryName().contains(keyword) && !product.getInventoryCode().contains(keyword)) {
            return false;
        }
        String brandName = trim(criteria.getBrandName());
        if (brandName != null && !brandName.equals(product.getBrandName())) {
            return false;
        }
        String templateCode = trim(criteria.getTemplateCode());
        return templateCode == null || templateCode.equals(product.getBoxTemplateCode());
    }

    private String emptyToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String trim(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
