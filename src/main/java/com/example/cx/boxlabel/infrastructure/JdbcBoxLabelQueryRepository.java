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
@Profile("sqlserver")
public class JdbcBoxLabelQueryRepository implements BoxLabelQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBoxLabelQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BoxLabelProductSummary> searchProducts(ProductSearchCriteria criteria) {
        String keyword = criteria == null || criteria.getKeyword() == null ? "" : criteria.getKeyword().trim();
        return jdbcTemplate.query(
                "SELECT TOP 50 CAST(T.id AS varchar(60)) AS productConfigId, " +
                        "T.cInvCode_hc AS inventoryCode, T.cInvName_hc AS inventoryName, " +
                        "BL.BL_NAME AS brandName, 'box-standard' AS boxTemplateCode, " +
                        "ISNULL(XT.PT_NAME, T.cPackageReportTemplate) AS boxTemplateName " +
                        "FROM UFDATA_001_2018.dbo.hsh_Name_Conversion T " +
                        "LEFT JOIN CD_BRAND_LOGO BL ON T.cCustomBrand = BL.BL_ID " +
                        "LEFT JOIN CD_PRINT_TEMPLATE XT ON T.cPackageReportConfig = XT.PT_ID " +
                        "WHERE (? = '' OR CAST(T.id AS varchar(60)) LIKE '%' + ? + '%' OR T.cInvName_hc LIKE '%' + ? + '%' OR T.cInvCode_hc LIKE '%' + ? + '%') " +
                        "ORDER BY T.cInvName_hc",
                new Object[]{keyword, keyword, keyword, keyword},
                productMapper()
        );
    }

    @Override
    public BoxLabelPrintRow findBoxLabelRow(BoxLabelPrintRequest request) {
        return jdbcTemplate.queryForObject(
                "SELECT TOP 1 " +
                        "'box-standard' AS templateCode, ISNULL(XT.PT_NAME, T.cPackageReportTemplate) AS boxTemplateName, " +
                        "ISNULL(DT.PT_NAME, T.cBagReportTemplate) AS bagTemplateName, ISNULL(T.cWatermark, '') AS watermark, " +
                        "CAST(T.cInvName_hc AS varchar(100)) AS inventoryName, T.cPackageSize AS packageSpec, " +
                        "ISNULL(T.cPackagingLabel, T.cInvName_hc) AS boxLabelName, BL.BL_NAME AS brandName, " +
                        "ISNULL(T.cAddMemo, '') AS addMemo, RTRIM(SM.SM_MODE) AS storageCondition, RTRIM(T.cShelfLife) AS shelfLife, " +
                        "T.nFlagPlace AS watermarkPlace, ISNULL(T.cCustomerBarCode, T.cGoodsBarCode) AS productBarcode, " +
                        "T.cGoodsBarCode AS originalProductBarcode, T.cCustomerBarCode AS originalCustomerBarcode, " +
                        "ISNULL(BL.BL_CONSIGNOR_NAME, '') AS consignorName, ISNULL(BL.BL_CONSIGNOR_PHONE, '') AS consignorPhone, " +
                        "ISNULL(BL.BL_CONSIGNOR_ADDRESS, '') AS consignorAddress, ISNULL(BL.BL_CONSIGNOR_TYPE, '委托商') AS consignorType, " +
                        "CASE WHEN T.cGoodsBarCode IS NULL THEN T.cBarCodeType ELSE 151 END AS barcodeType, " +
                        "BA.BA_PHONE AS contact, BA.BA_FULL_NAME AS company, BA.BA_ADDRESS AS address, " +
                        "ISNULL(T.cCompanyTypeName, '生产商') AS companyTypeName, BA.BA_PLACE_OF_ORIGIN AS origin, " +
                        "BA.BA_PRODUCTION_LICENSE_NO AS productionLicenseNo, BPS.BPS_CODE AS productStandardNo, " +
                        "'配料表：' + ISNULL(T.cIngredientsList, '') AS ingredients, T.cNutritionFacts AS nutritionFacts, " +
                        "T.cNetContent AS netContent, T.cProductClass AS productClass, T.cInstructions AS instructions, " +
                        "T.cAllergenInfo AS allergenInfo, T.cKindReminder AS reminder, BL.BL_LONG_LOGO AS longLogo, " +
                        "NULL AS logoPath, 2 AS logoSize, T.lShelfLifeConvert AS shelfLifeConvert " +
                        "FROM UFDATA_001_2018.dbo.hsh_Name_Conversion T " +
                        "LEFT JOIN CD_STORE_MODE SM ON SM.SM_ID = T.cStoreMode " +
                        "LEFT JOIN CD_PRINT_TEMPLATE DT ON T.cBagReportConfig = DT.PT_ID " +
                        "LEFT JOIN CD_PRINT_TEMPLATE XT ON T.cPackageReportConfig = XT.PT_ID " +
                        "LEFT JOIN CD_BRAND_LOGO BL ON T.cCustomBrand = BL.BL_ID " +
                        "LEFT JOIN hsh_Name_Conversion_Product_Standard PS ON PS.PS_NC_ID = T.id " +
                        "LEFT JOIN CD_BUSINESS_PRODUCT_STANDARD BPS ON PS.PS_BPS_ID = BPS.BPS_ID " +
                        "LEFT JOIN CD_BUSINESS_ADDRESS BA ON BPS.BPS_BA_ID = BA.BA_ID " +
                        "WHERE CAST(T.id AS varchar(60)) = ?",
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
                row.setLongLogo((Boolean) rs.getObject("longLogo"));
                row.setLogoPath(rs.getString("logoPath"));
                row.setLogoSize((Integer) rs.getObject("logoSize"));
                row.setShelfLifeConvert((Boolean) rs.getObject("shelfLifeConvert"));
                return row;
            }
        };
    }
}
