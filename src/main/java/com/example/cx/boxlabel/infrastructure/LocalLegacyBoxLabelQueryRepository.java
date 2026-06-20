package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Profile("!sqlserver")
public class LocalLegacyBoxLabelQueryRepository implements BoxLabelQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public LocalLegacyBoxLabelQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BoxLabelProductSummary> searchProducts(ProductSearchCriteria criteria) {
        String keyword = criteria == null || criteria.getKeyword() == null ? "" : criteria.getKeyword().trim();
        String brandName = criteria == null || criteria.getBrandName() == null ? "" : criteria.getBrandName().trim();
        String templateCode = criteria == null || criteria.getTemplateCode() == null ? "" : criteria.getTemplateCode().trim();
        return jdbcTemplate.query(
                "SELECT T.id AS productConfigId, " +
                        "T.cInvCode_hc AS inventoryCode, T.cInvName_hc AS inventoryName, " +
                        "BL.BL_NAME AS brandName, 'box-standard' AS boxTemplateCode, " +
                        "COALESCE(XT.PT_NAME, T.cPackageReportTemplate) AS boxTemplateName " +
                        "FROM hsh_Name_Conversion T " +
                        "LEFT JOIN Inventory I ON T.cInvCode_hc = I.cInvCode " +
                        "LEFT JOIN CD_BRAND_LOGO BL ON T.cCustomBrand = BL.BL_ID " +
                        "LEFT JOIN CD_PRINT_TEMPLATE XT ON T.cPackageReportConfig = XT.PT_ID " +
                        "WHERE (? = '' OR T.cInvName_hc LIKE '%' || ? || '%' OR T.cInvCode_hc LIKE '%' || ? || '%') " +
                        "AND (? = '' OR BL.BL_NAME = ?) " +
                        "AND (? = '' OR 'box-standard' = ?) " +
                        "ORDER BY T.cInvName_hc",
                new Object[]{keyword, keyword, keyword, brandName, brandName, templateCode, templateCode},
                productMapper()
        );
    }

    @Override
    public BoxLabelPrintRow findBoxLabelRow(BoxLabelPrintRequest request) {
        return jdbcTemplate.queryForObject(
                "SELECT " +
                        "'box-standard' AS templateCode, COALESCE(XT.PT_NAME, T.cPackageReportTemplate) AS boxTemplateName, " +
                        "COALESCE(DT.PT_NAME, T.cBagReportTemplate) AS bagTemplateName, COALESCE(T.cWatermark, '') AS watermark, " +
                        "T.cInvName_hc AS inventoryName, T.cPackageSize AS packageSpec, " +
                        "COALESCE(T.cPackagingLabel, T.cInvName_hc) AS boxLabelName, BL.BL_NAME AS brandName, " +
                        "COALESCE(T.cAddMemo, '') AS addMemo, TRIM(SM.SM_MODE) AS storageCondition, TRIM(T.cShelfLife) AS shelfLife, " +
                        "T.nFlagPlace AS watermarkPlace, COALESCE(NULLIF(T.cCustomerBarCode, ''), T.cGoodsBarCode) AS productBarcode, " +
                        "T.cGoodsBarCode AS originalProductBarcode, T.cCustomerBarCode AS originalCustomerBarcode, " +
                        "COALESCE(BL.BL_CONSIGNOR_NAME, '') AS consignorName, COALESCE(BL.BL_CONSIGNOR_PHONE, '') AS consignorPhone, " +
                        "COALESCE(BL.BL_CONSIGNOR_ADDRESS, '') AS consignorAddress, COALESCE(BL.BL_CONSIGNOR_TYPE, 'Consignor') AS consignorType, " +
                        "COALESCE(T.cBarCodeType, 151) AS barcodeType, " +
                        "BA.BA_PHONE AS contact, BA.BA_FULL_NAME AS company, BA.BA_ADDRESS AS address, " +
                        "COALESCE(T.cCompanyTypeName, 'Manufacturer') AS companyTypeName, BA.BA_PLACE_OF_ORIGIN AS origin, " +
                        "BA.BA_PRODUCTION_LICENSE_NO AS productionLicenseNo, BPS.BPS_CODE AS productStandardNo, " +
                        "'Ingredients: ' || COALESCE(T.cIngredientsList, '') AS ingredients, T.cNutritionFacts AS nutritionFacts, " +
                        "T.cNetContent AS netContent, T.cProductClass AS productClass, T.cInstructions AS instructions, " +
                        "T.cAllergenInfo AS allergenInfo, T.cKindReminder AS reminder, BL.BL_LONG_LOGO AS longLogo, " +
                        "'' AS logoPath, 2 AS logoSize, COALESCE(T.lShelfLifeConvert, 0) AS shelfLifeConvert " +
                        "FROM hsh_Name_Conversion T " +
                        "LEFT JOIN Inventory I ON T.cInvCode_hc = I.cInvCode " +
                        "LEFT JOIN CD_STORE_MODE SM ON SM.SM_ID = T.cStoreMode " +
                        "LEFT JOIN CD_PRINT_TEMPLATE DT ON T.cBagReportConfig = DT.PT_ID " +
                        "LEFT JOIN CD_PRINT_TEMPLATE XT ON T.cPackageReportConfig = XT.PT_ID " +
                        "LEFT JOIN CD_BRAND_LOGO BL ON T.cCustomBrand = BL.BL_ID " +
                        "LEFT JOIN hsh_Name_Conversion_Product_Standard PS ON PS.PS_NC_ID = T.id " +
                        "LEFT JOIN CD_BUSINESS_PRODUCT_STANDARD BPS ON PS.PS_BPS_ID = BPS.BPS_ID " +
                        "LEFT JOIN CD_BUSINESS_ADDRESS BA ON BPS.BPS_BA_ID = BA.BA_ID " +
                        "WHERE T.id = ?",
                new Object[]{request.getProductConfigId()},
                rowMapper()
        );
    }

    private RowMapper<BoxLabelProductSummary> productMapper() {
        return new RowMapper<BoxLabelProductSummary>() {
            @Override
            public BoxLabelProductSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
                BoxLabelProductSummary summary = new BoxLabelProductSummary();
                summary.setProductConfigId(rs.getString("productConfigId"));
                summary.setInventoryCode(rs.getString("inventoryCode"));
                summary.setInventoryName(rs.getString("inventoryName"));
                summary.setBrandName(rs.getString("brandName"));
                summary.setBoxTemplateCode(rs.getString("boxTemplateCode"));
                summary.setBoxTemplateName(rs.getString("boxTemplateName"));
                return summary;
            }
        };
    }

    private RowMapper<BoxLabelPrintRow> rowMapper() {
        return new RowMapper<BoxLabelPrintRow>() {
            @Override
            public BoxLabelPrintRow mapRow(ResultSet rs, int rowNum) throws SQLException {
                BoxLabelPrintRow row = new BoxLabelPrintRow();
                row.setTemplateCode(rs.getString("templateCode"));
                row.setBoxTemplateName(rs.getString("boxTemplateName"));
                row.setBagTemplateName(rs.getString("bagTemplateName"));
                row.setWatermark(rs.getString("watermark"));
                row.setInventoryName(rs.getString("inventoryName"));
                row.setPackageSpec(rs.getString("packageSpec"));
                row.setBoxLabelName(rs.getString("boxLabelName"));
                row.setBrandName(rs.getString("brandName"));
                row.setAddMemo(rs.getString("addMemo"));
                row.setStorageCondition(rs.getString("storageCondition"));
                row.setShelfLife(rs.getString("shelfLife"));
                row.setWatermarkPlace((Integer) rs.getObject("watermarkPlace"));
                row.setQrCode("FA4C5A54-0000-0000-0000-326133653233");
                row.setProductBarcode(rs.getString("productBarcode"));
                row.setOriginalProductBarcode(rs.getString("originalProductBarcode"));
                row.setOriginalCustomerBarcode(rs.getString("originalCustomerBarcode"));
                row.setConsignorName(rs.getString("consignorName"));
                row.setConsignorPhone(rs.getString("consignorPhone"));
                row.setConsignorAddress(rs.getString("consignorAddress"));
                row.setConsignorType(rs.getString("consignorType"));
                row.setBarcodeType((Integer) rs.getObject("barcodeType"));
                row.setContact(rs.getString("contact"));
                row.setCompany(rs.getString("company"));
                row.setAddress(rs.getString("address"));
                row.setCompanyTypeName(rs.getString("companyTypeName"));
                row.setOrigin(rs.getString("origin"));
                row.setProductionLicenseNo(rs.getString("productionLicenseNo"));
                row.setProductStandardNo(rs.getString("productStandardNo"));
                row.setIngredients(rs.getString("ingredients"));
                row.setNutritionFacts(rs.getString("nutritionFacts"));
                row.setNetContent(rs.getString("netContent"));
                row.setProductClass(rs.getString("productClass"));
                row.setInstructions(rs.getString("instructions"));
                row.setAllergenInfo(rs.getString("allergenInfo"));
                row.setReminder(rs.getString("reminder"));
                row.setLongLogo(rs.getBoolean("longLogo"));
                row.setLogoPath(rs.getString("logoPath"));
                row.setLogoSize((Integer) rs.getObject("logoSize"));
                row.setShelfLifeConvert(rs.getBoolean("shelfLifeConvert"));
                return row;
            }
        };
    }
}
