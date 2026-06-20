package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.BoxLabelFieldMapping;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BoxLabelFieldContractService {

    public List<BoxLabelFieldMapping> listMappings() {
        List<BoxLabelFieldMapping> mappings = new ArrayList<BoxLabelFieldMapping>();
        mappings.add(required("箱贴模版", "boxTemplateName", "String", "CD_PRINT_TEMPLATE.PT_NAME / cPackageReportTemplate", "Legacy template name retained as data metadata; Jasper template selection uses templateCode."));
        mappings.add(optional("袋贴模版", "bagTemplateName", "String", "CD_PRINT_TEMPLATE.PT_NAME / cBagReportTemplate", "Kept because the legacy cursor contains both box and bag templates."));
        mappings.add(optional("水印", "watermark", "String", "TB_BAG_STICKER_REPORT.cWatermark", "Blank string is valid."));
        mappings.add(required("班次", "shift", "String", "Request / legacy lcBc", "Printed on the label when the template references it."));
        mappings.add(required("生产日期", "productionDate", "String", "Request / legacy lcRQ", "Keep legacy date formatting until renderer replacement is proven."));
        mappings.add(required("存货名称", "inventoryName", "String", "UFDATA_001_2018.dbo.Inventory", "Fallback source for label title."));
        mappings.add(required("装箱规格", "packageSpec", "String", "hsh_Name_Conversion / legacy lcZXGG", "Jasper layout-sensitive field."));
        mappings.add(required("箱贴名称", "boxLabelName", "String", "cPackagingLabel / inventoryName fallback", "Computed by legacy GetTitle logic."));
        mappings.add(optional("定制品牌", "brandName", "String", "CD_BRAND_LOGO / brand remark", "May be blank."));
        mappings.add(optional("附加备注", "addMemo", "String", "cAddMemo", "Legacy NVL converts null to blank."));
        mappings.add(required("贮存条件", "storageCondition", "String", "CD_STORE_MODE.SM_MODE", "Printed storage condition."));
        mappings.add(required("保质期", "shelfLife", "String", "cShelfLife / conversion logic", "Watch lShelfLifeConvert behavior."));
        mappings.add(optional("水印位置", "watermarkPlace", "Integer", "nFlagPlace", "Integer flag used by templates."));
        mappings.add(required("二维码", "qrCode", "String", "Legacy fixed UUID / request", "QR payload candidate."));
        mappings.add(required("产品条码", "productBarcode", "String", "cGoodsBarCode / cCustomerBarCode", "Code128 scanability must be accepted."));
        mappings.add(optional("原产品条码", "originalProductBarcode", "String", "TB_BAG_STICKER_REPORT", "Added by legacy cursor path."));
        mappings.add(optional("原客户条码", "originalCustomerBarcode", "String", "TB_BAG_STICKER_REPORT", "Added by legacy cursor path."));
        mappings.add(optional("委托商", "consignorName", "String", "CD_BRAND_LOGO consignor fields", "May be split by nConsignorSubsidiary."));
        mappings.add(optional("委托商电话", "consignorPhone", "String", "CD_BRAND_LOGO consignor fields", "May be split by nConsignorSubsidiary."));
        mappings.add(optional("委托商地址", "consignorAddress", "String", "CD_BRAND_LOGO consignor fields", "May be split by nConsignorSubsidiary."));
        mappings.add(optional("委托类型", "consignorType", "String", "BL_CONSIGNOR_TYPE", "Defaults to 委托商 in legacy SQL."));
        mappings.add(required("条码类型", "barcodeType", "Integer", "cBarCodeType / cGoodsBarCode logic", "Legacy fallback often uses 151."));
        mappings.add(required("联系方式", "contact", "String", "CD_BUSINESS_ADDRESS / consignor fallback", "Printed contact field."));
        mappings.add(required("公司", "company", "String", "CD_BUSINESS_ADDRESS", "Printed producer/company field."));
        mappings.add(required("地址", "address", "String", "CD_BUSINESS_ADDRESS", "Layout-sensitive address field."));
        mappings.add(optional("公司类型名称", "companyTypeName", "String", "CD_BUSINESS_ADDRESS", "Printed label qualifier."));
        mappings.add(optional("产地", "origin", "String", "CD_BUSINESS_ADDRESS / product data", "May be blank."));
        mappings.add(required("生产许可证号", "productionLicenseNo", "String", "CD_BUSINESS_ADDRESS.BA_PRODUCTION_LICENSE_NO", "Regulatory field."));
        mappings.add(required("产品标准号", "productStandardNo", "String", "CD_BUSINESS_PRODUCT_STANDARD.BPS_CODE", "Regulatory field."));
        mappings.add(required("配料表", "ingredients", "String", "cIngredientsList", "Legacy SQL prepends 配料表：."));
        mappings.add(optional("营养成份", "nutritionFacts", "String", "cNutritionFacts", "May be long."));
        mappings.add(required("净含量", "netContent", "String", "cNetContent / legacy lcJHL", "Printed field."));
        mappings.add(optional("产品类别", "productClass", "String", "cProductClass", "May be blank."));
        mappings.add(optional("食用说明", "instructions", "String", "cInstructions", "May be blank."));
        mappings.add(optional("致敏原", "allergenInfo", "String", "cAllergenInfo", "May be blank."));
        mappings.add(optional("温馨提示", "reminder", "String", "cKindReminder", "May be blank."));
        mappings.add(optional("BL_LONG_LOGO", "longLogo", "Boolean", "CD_BRAND_LOGO.BL_LONG_LOGO", "Controls logo layout variant."));
        mappings.add(optional("LOGO", "logoPath", "String", "Configured Java logo storage", "Blank when no logo should be rendered."));
        mappings.add(optional("LOGO_SIZE", "logoSize", "Integer", "BL_LOGN_WIDTH / BL_LONG_HEIGHT logic", "Affects logo placement."));
        return Collections.unmodifiableList(mappings);
    }

    private BoxLabelFieldMapping required(String vfpFieldName, String javaFieldName, String javaType, String source, String note) {
        return new BoxLabelFieldMapping(vfpFieldName, javaFieldName, javaType, true, source, note);
    }

    private BoxLabelFieldMapping optional(String vfpFieldName, String javaFieldName, String javaType, String source, String note) {
        return new BoxLabelFieldMapping(vfpFieldName, javaFieldName, javaType, false, source, note);
    }
}
