package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.NameConversionDetail;
import com.example.cx.boxlabel.domain.NameConversionFieldSource;
import com.example.cx.boxlabel.domain.ProductTemplateBinding;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;
import com.example.cx.boxlabel.domain.SearchResult;
import com.example.cx.boxlabel.infrastructure.BoxLabelQueryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NameConversionService {

    private final BoxLabelQueryRepository queryRepository;
    private final ProductTemplateBindingService bindingService;
    private final BoxLabelTemplateService templateService;

    public NameConversionService(BoxLabelQueryRepository queryRepository,
                                 ProductTemplateBindingService bindingService,
                                 BoxLabelTemplateService templateService) {
        this.queryRepository = queryRepository;
        this.bindingService = bindingService;
        this.templateService = templateService;
    }

    public SearchResult<BoxLabelProductSummary> searchProducts(ProductSearchCriteria criteria) {
        ProductSearchCriteria legacyCriteria = legacyCriteria(criteria);
        List<BoxLabelProductSummary> summaries = new ArrayList<BoxLabelProductSummary>();
        for (BoxLabelProductSummary summary : queryRepository.searchProducts(legacyCriteria)) {
            BoxLabelProductSummary enriched = withJavaBinding(summary);
            if (matchesTemplate(criteria, enriched.getBoxTemplateCode())) {
                summaries.add(enriched);
            }
        }
        return new SearchResult<BoxLabelProductSummary>(summaries);
    }

    public NameConversionDetail detail(String productConfigId) {
        BoxLabelPrintRequest request = new BoxLabelPrintRequest();
        request.setProductConfigId(productConfigId);
        request.setTemplateCode("box-standard");
        BoxLabelPrintRow row = queryRepository.findBoxLabelRow(request);

        NameConversionDetail detail = new NameConversionDetail();
        detail.setProductConfigId(productConfigId);
        detail.setInventoryCode(findInventoryCode(productConfigId));
        detail.setInventoryName(row.getInventoryName());
        detail.setBrandName(row.getBrandName());
        detail.setTemplateBinding(bindingService.getBinding(productConfigId));
        addSources(detail, row);
        return detail;
    }

    private String findInventoryCode(String productConfigId) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        for (BoxLabelProductSummary summary : queryRepository.searchProducts(criteria)) {
            if (productConfigId.equals(summary.getProductConfigId())) {
                return summary.getInventoryCode();
            }
        }
        return null;
    }

    private void addSources(NameConversionDetail detail, BoxLabelPrintRow row) {
        ProductTemplateBinding binding = detail.getTemplateBinding();
        addSource(detail, "模板与绑定", "Java 箱贴模板", "boxTemplateCode", "LP_PRODUCT_TEMPLATE_BINDING.box_template_code", binding.getBoxTemplateCode());
        addSource(detail, "模板与绑定", "Java 袋贴模板", "bagTemplateCode", "LP_PRODUCT_TEMPLATE_BINDING.bag_template_code", binding.getBagTemplateCode());
        addSource(detail, "模板与绑定", "旧箱贴模板名", "boxTemplateName", "CD_PRINT_TEMPLATE.PT_NAME / hsh_Name_Conversion.cPackageReportConfig", row.getBoxTemplateName());
        addSource(detail, "模板与绑定", "旧袋贴模板名", "bagTemplateName", "CD_PRINT_TEMPLATE.PT_NAME / hsh_Name_Conversion.cBagReportConfig", row.getBagTemplateName());

        addSource(detail, "产品基础", "存货名称", "inventoryName", "hsh_Name_Conversion.cInvName_hc / Inventory.cInvName", row.getInventoryName());
        addSource(detail, "产品基础", "箱贴名称", "boxLabelName", "hsh_Name_Conversion.cPackagingLabel", row.getBoxLabelName());
        addSource(detail, "产品基础", "装箱规格", "packageSpec", "hsh_Name_Conversion.cPackageSize", row.getPackageSpec());
        addSource(detail, "产品基础", "净含量", "netContent", "hsh_Name_Conversion.cNetContent", row.getNetContent());
        addSource(detail, "产品基础", "产品类别", "productClass", "hsh_Name_Conversion.cProductClass", row.getProductClass());

        addSource(detail, "品牌与 Logo", "定制品牌", "brandName", "CD_BRAND_LOGO.BL_NAME", row.getBrandName());
        addSource(detail, "品牌与 Logo", "品牌备注", "addMemo", "hsh_Name_Conversion.cAddMemo", row.getAddMemo());
        addSource(detail, "品牌与 Logo", "水印", "watermark", "hsh_Name_Conversion.cWatermark / cAddMemo rule", row.getWatermark());
        addSource(detail, "品牌与 Logo", "水印位置", "watermarkPlace", "hsh_Name_Conversion.nFlagPlace", row.getWatermarkPlace());
        addSource(detail, "品牌与 Logo", "Logo 路径", "logoPath", "CD_BRAND_LOGO file resolver", row.getLogoPath());
        addSource(detail, "品牌与 Logo", "Logo 尺寸", "logoSize", "CD_BRAND_LOGO / template default", row.getLogoSize());
        addSource(detail, "品牌与 Logo", "是否长 Logo", "longLogo", "CD_BRAND_LOGO.BL_LONG_LOGO", row.getLongLogo());

        addSource(detail, "生产与资质", "公司", "company", "CD_BUSINESS_ADDRESS.BA_FULL_NAME", row.getCompany());
        addSource(detail, "生产与资质", "公司类型", "companyTypeName", "hsh_Name_Conversion.cCompanyTypeName", row.getCompanyTypeName());
        addSource(detail, "生产与资质", "联系电话", "contact", "CD_BUSINESS_ADDRESS.BA_PHONE", row.getContact());
        addSource(detail, "生产与资质", "地址", "address", "CD_BUSINESS_ADDRESS.BA_ADDRESS", row.getAddress());
        addSource(detail, "生产与资质", "产地", "origin", "CD_BUSINESS_ADDRESS.BA_PLACE_OF_ORIGIN", row.getOrigin());
        addSource(detail, "生产与资质", "生产许可证号", "productionLicenseNo", "CD_BUSINESS_ADDRESS.BA_PRODUCTION_LICENSE_NO", row.getProductionLicenseNo());
        addSource(detail, "生产与资质", "产品标准号", "productStandardNo", "CD_BUSINESS_PRODUCT_STANDARD.BPS_CODE", row.getProductStandardNo());

        addSource(detail, "储存与说明", "贮存条件", "storageCondition", "CD_STORE_MODE.SM_MODE", row.getStorageCondition());
        addSource(detail, "储存与说明", "保质期", "shelfLife", "hsh_Name_Conversion.cShelfLife", row.getShelfLife());
        addSource(detail, "储存与说明", "保质期转日期", "shelfLifeConvert", "hsh_Name_Conversion.lShelfLifeConvert", row.getShelfLifeConvert());
        addSource(detail, "储存与说明", "食用说明", "instructions", "hsh_Name_Conversion.cInstructions", row.getInstructions());
        addSource(detail, "储存与说明", "致敏原", "allergenInfo", "hsh_Name_Conversion.cAllergenInfo", row.getAllergenInfo());
        addSource(detail, "储存与说明", "温馨提示", "reminder", "hsh_Name_Conversion.cKindReminder", row.getReminder());

        addSource(detail, "条码与追溯", "最终条码", "productBarcode", "customer barcode fallback to goods barcode", row.getProductBarcode());
        addSource(detail, "条码与追溯", "原产品条码", "originalProductBarcode", "hsh_Name_Conversion.cGoodsBarCode", row.getOriginalProductBarcode());
        addSource(detail, "条码与追溯", "原客户条码", "originalCustomerBarcode", "hsh_Name_Conversion.cCustomerBarCode", row.getOriginalCustomerBarcode());
        addSource(detail, "条码与追溯", "条码类型", "barcodeType", "hsh_Name_Conversion.cBarCodeType", row.getBarcodeType());
        addSource(detail, "条码与追溯", "二维码 payload", "qrCode", "FC_PACKAGE_CODE / Java fallback", row.getQrCode());

        addSource(detail, "委托商", "委托商", "consignorName", "CD_BRAND_LOGO.BL_CONSIGNOR_NAME", row.getConsignorName());
        addSource(detail, "委托商", "委托商电话", "consignorPhone", "CD_BRAND_LOGO.BL_CONSIGNOR_PHONE", row.getConsignorPhone());
        addSource(detail, "委托商", "委托商地址", "consignorAddress", "CD_BRAND_LOGO.BL_CONSIGNOR_ADDRESS", row.getConsignorAddress());
        addSource(detail, "委托商", "委托类型", "consignorType", "CD_BRAND_LOGO.BL_CONSIGNOR_TYPE", row.getConsignorType());

        addSource(detail, "配方与营养", "配料表", "ingredients", "hsh_Name_Conversion.cIngredientsList", row.getIngredients());
        addSource(detail, "配方与营养", "营养成份", "nutritionFacts", "hsh_Name_Conversion.cNutritionFacts", row.getNutritionFacts());
    }

    private void addSource(NameConversionDetail detail, String category, String displayName, String fieldName, String source, Object value) {
        NameConversionFieldSource fieldSource = new NameConversionFieldSource(
                category,
                displayName,
                fieldName,
                source,
                value == null ? "" : String.valueOf(value)
        );
        applyDiagnostics(fieldSource);
        detail.getFieldSources().add(fieldSource);
    }

    private void applyDiagnostics(NameConversionFieldSource fieldSource) {
        String sourceType = resolveSourceType(fieldSource.getSource());
        String fieldStatus = resolveFieldStatus(sourceType, fieldSource.getValue());
        fieldSource.setSourceType(sourceType);
        fieldSource.setFieldStatus(fieldStatus);
        fieldSource.setStatusText(statusText(fieldStatus));
    }

    private String resolveSourceType(String source) {
        String text = source == null ? "" : source;
        String lower = text.toLowerCase();
        if (lower.contains("template default")) {
            return "TEMPLATE_DEFAULT";
        }
        if (lower.contains("file resolver")) {
            return "FILE_RESOLVER";
        }
        if (lower.contains("fallback")) {
            return "FALLBACK_RULE";
        }
        if (text.startsWith("LP_")) {
            return "JAVA_BINDING";
        }
        if (text.startsWith("CD_") || text.startsWith("hsh_") || text.startsWith("Inventory")) {
            return "LEGACY_TABLE";
        }
        return "COMPUTED";
    }

    private String resolveFieldStatus(String sourceType, String value) {
        if (!hasText(value)) {
            return "MISSING";
        }
        if ("FALLBACK_RULE".equals(sourceType) || "TEMPLATE_DEFAULT".equals(sourceType)) {
            return "FALLBACK";
        }
        return "OK";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String statusText(String fieldStatus) {
        if ("MISSING".equals(fieldStatus)) {
            return "\u7f3a\u5931";
        }
        if ("FALLBACK".equals(fieldStatus)) {
            return "\u56de\u9000";
        }
        return "\u5df2\u914d\u7f6e";
    }

    private ProductSearchCriteria legacyCriteria(ProductSearchCriteria criteria) {
        ProductSearchCriteria copy = new ProductSearchCriteria();
        if (criteria != null) {
            copy.setKeyword(criteria.getKeyword());
            copy.setBrandName(criteria.getBrandName());
        }
        return copy;
    }

    private BoxLabelProductSummary withJavaBinding(BoxLabelProductSummary source) {
        BoxLabelProductSummary copy = new BoxLabelProductSummary();
        copy.setProductConfigId(source.getProductConfigId());
        copy.setInventoryCode(source.getInventoryCode());
        copy.setInventoryName(source.getInventoryName());
        copy.setBrandName(source.getBrandName());

        ProductTemplateBinding binding = bindingService.getBinding(source.getProductConfigId());
        LabelTemplate template = templateService.requireTemplate(binding.getBoxTemplateCode());
        copy.setBoxTemplateCode(template.getCode());
        copy.setBoxTemplateName(template.getName());
        return copy;
    }

    private boolean matchesTemplate(ProductSearchCriteria criteria, String templateCode) {
        if (criteria == null || criteria.getTemplateCode() == null || criteria.getTemplateCode().trim().isEmpty()) {
            return true;
        }
        return criteria.getTemplateCode().trim().equals(templateCode);
    }
}
