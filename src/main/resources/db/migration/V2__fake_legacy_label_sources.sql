CREATE TABLE Inventory (
    cInvCode VARCHAR(60) NOT NULL PRIMARY KEY,
    cInvName VARCHAR(200) NOT NULL
);

CREATE TABLE CD_PRINT_TEMPLATE (
    PT_ID VARCHAR(60) NOT NULL PRIMARY KEY,
    PT_NAME VARCHAR(200) NOT NULL
);

CREATE TABLE CD_BRAND_LOGO (
    BL_ID VARCHAR(60) NOT NULL PRIMARY KEY,
    BL_NAME VARCHAR(200) NOT NULL,
    BL_CONSIGNOR_NAME VARCHAR(200),
    BL_CONSIGNOR_PHONE VARCHAR(80),
    BL_CONSIGNOR_ADDRESS VARCHAR(260),
    BL_CONSIGNOR_TYPE VARCHAR(80),
    BL_LONG_LOGO BIT NOT NULL
);

CREATE TABLE CD_STORE_MODE (
    SM_ID VARCHAR(60) NOT NULL PRIMARY KEY,
    SM_MODE VARCHAR(200) NOT NULL
);

CREATE TABLE CD_BUSINESS_ADDRESS (
    BA_ID VARCHAR(60) NOT NULL PRIMARY KEY,
    BA_PHONE VARCHAR(80),
    BA_FULL_NAME VARCHAR(200),
    BA_ADDRESS VARCHAR(260),
    BA_PLACE_OF_ORIGIN VARCHAR(120),
    BA_PRODUCTION_LICENSE_NO VARCHAR(120)
);

CREATE TABLE CD_BUSINESS_PRODUCT_STANDARD (
    BPS_ID VARCHAR(60) NOT NULL PRIMARY KEY,
    BPS_CODE VARCHAR(120) NOT NULL,
    BPS_BA_ID VARCHAR(60) NOT NULL
);

CREATE TABLE hsh_Name_Conversion_Product_Standard (
    PS_ID VARCHAR(60) NOT NULL PRIMARY KEY,
    PS_NC_ID VARCHAR(80) NOT NULL,
    PS_BPS_ID VARCHAR(60) NOT NULL
);

CREATE TABLE hsh_Name_Conversion (
    id VARCHAR(80) NOT NULL PRIMARY KEY,
    cInvCode_hc VARCHAR(60) NOT NULL,
    cInvName_hc VARCHAR(200) NOT NULL,
    cPackageReportConfig VARCHAR(60),
    cBagReportConfig VARCHAR(60),
    cPackageReportTemplate VARCHAR(200),
    cBagReportTemplate VARCHAR(200),
    cWatermark VARCHAR(200),
    cPackageSize VARCHAR(120),
    cPackagingLabel VARCHAR(200),
    cCustomBrand VARCHAR(60),
    cAddMemo VARCHAR(500),
    cStoreMode VARCHAR(60),
    cShelfLife VARCHAR(80),
    nFlagPlace INT,
    cCustomerBarCode VARCHAR(80),
    cGoodsBarCode VARCHAR(80),
    cBarCodeType INT,
    cCompanyTypeName VARCHAR(120),
    cIngredientsList VARCHAR(1000),
    cNutritionFacts VARCHAR(1000),
    cNetContent VARCHAR(200),
    cProductClass VARCHAR(200),
    cInstructions VARCHAR(1000),
    cAllergenInfo VARCHAR(1000),
    cKindReminder VARCHAR(1000),
    lShelfLifeConvert BIT
);

INSERT INTO Inventory (cInvCode, cInvName) VALUES
    ('DEMO001', '示例产品'),
    ('SQL002', 'SQL Legacy Product');

INSERT INTO CD_PRINT_TEMPLATE (PT_ID, PT_NAME) VALUES
    ('BOX-LEGACY', 'Legacy Box Template'),
    ('BAG-LEGACY', 'Legacy Bag Template');

INSERT INTO CD_BRAND_LOGO (
    BL_ID, BL_NAME, BL_CONSIGNOR_NAME, BL_CONSIGNOR_PHONE,
    BL_CONSIGNOR_ADDRESS, BL_CONSIGNOR_TYPE, BL_LONG_LOGO
) VALUES
    ('BRAND-DEMO', 'Demo Brand', '示例食品委托商有限公司', '021-60000001', '上海市浦东新区示例路 168 号', '委托方', 0),
    ('BRAND-SQL', 'SQL Legacy Brand', 'SQL Legacy Consignor', '000-00000002', 'No. 2 Consignor Road', 'Consignor', 0);

INSERT INTO CD_STORE_MODE (SM_ID, SM_MODE) VALUES
    ('FROZEN', '-18℃以下冷冻保存，运输过程保持冷链'),
    ('NORMAL', 'Keep in cool and dry place');

INSERT INTO CD_BUSINESS_ADDRESS (
    BA_ID, BA_PHONE, BA_FULL_NAME, BA_ADDRESS, BA_PLACE_OF_ORIGIN, BA_PRODUCTION_LICENSE_NO
) VALUES
    ('BA-DEMO', '400-000-0001', '示例食品制造有限公司', '江苏省苏州市工业园区样张大道 88 号', '江苏 苏州', 'SC12345678900001'),
    ('BA-SQL', '400-000-0002', 'SQL Legacy Foods Co., Ltd.', 'No. 2 Legacy Road', 'China', 'SC12345678900002');

INSERT INTO CD_BUSINESS_PRODUCT_STANDARD (BPS_ID, BPS_CODE, BPS_BA_ID) VALUES
    ('BPS-DEMO', 'Q/SQL 0001S', 'BA-DEMO'),
    ('BPS-SQL', 'Q/SQL 0002S', 'BA-SQL');

INSERT INTO hsh_Name_Conversion_Product_Standard (PS_ID, PS_NC_ID, PS_BPS_ID) VALUES
    ('PS-DEMO', 'DEMO-BOX-001', 'BPS-DEMO'),
    ('PS-SQL', 'SQL-BOX-002', 'BPS-SQL');

INSERT INTO hsh_Name_Conversion (
    id, cInvCode_hc, cInvName_hc, cPackageReportConfig, cBagReportConfig,
    cPackageReportTemplate, cBagReportTemplate, cWatermark, cPackageSize,
    cPackagingLabel, cCustomBrand, cAddMemo, cStoreMode, cShelfLife,
    nFlagPlace, cCustomerBarCode, cGoodsBarCode, cBarCodeType,
    cCompanyTypeName, cIngredientsList, cNutritionFacts, cNetContent,
    cProductClass, cInstructions, cAllergenInfo, cKindReminder, lShelfLifeConvert
) VALUES
    ('DEMO-BOX-001', 'DEMO001', '示例产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '内部测试样张', '1kg x 10袋/箱',
     '示例产品箱贴', 'BRAND-DEMO', '', 'FROZEN', '12 months',
     0, '6900000000000', '6900000000000', 151,
     '生产商', '小麦粉、猪肉、鸡肉、饮用水、洋葱、食用盐、酱油、香辛料', '营养成份表：每100g 能量 890kJ，蛋白质 8.6g，脂肪 6.2g，碳水化合物 28.5g，钠 520mg', '净含量：1kg（100g x 10袋）',
     '速冻调制食品', '食用方法：无需解冻，沸水蒸煮或蒸制至中心熟透后食用。', '含小麦、大豆及其制品；生产线亦加工含蛋、乳成分产品。', '请于-18℃以下冷冻保存，开封后请尽快食用。', 0),
    ('SQL-BOX-002', 'SQL002', 'SQL Legacy Product', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '', '2kg*6 bags/carton',
     'SQL Legacy Product Box Label', 'BRAND-SQL', 'legacy memo', 'FROZEN', '18 months',
     0, '6950000000002', '6950000000002', 151,
     'Manufacturer', 'sample flour, water', 'Nutrition facts: sample data', 'Net content: 2kg',
     'Frozen food', 'Heat before eating', 'None', 'Keep frozen until cooking', 0);
