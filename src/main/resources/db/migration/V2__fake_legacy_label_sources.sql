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

INSERT INTO Inventory (cInvCode, cInvName) VALUES
    ('DMBX001', 'DEMO-BOX 普通箱贴产品'),
    ('DMBX002', 'DEMO-BOX 委托商箱贴产品'),
    ('DMBX003', 'DEMO-BOX 商超专供箱贴产品'),
    ('DMBX004', 'DEMO-BOX 125规格箱贴产品'),
    ('DMBX005', 'DEMO-BOX 长文本箱贴产品'),
    ('DMBX006', 'DEMO-BOX 缺失资料箱贴产品');

INSERT INTO CD_BRAND_LOGO (
    BL_ID, BL_NAME, BL_CONSIGNOR_NAME, BL_CONSIGNOR_PHONE,
    BL_CONSIGNOR_ADDRESS, BL_CONSIGNOR_TYPE, BL_LONG_LOGO
) VALUES
    ('BRAND-NORMAL', '标准演示品牌', '', '', '', '', 0),
    ('BRAND-CONSIGNOR', '委托演示品牌', '上海样张食品委托有限公司', '021-61000002', '上海市闵行区协同路 66 号', '委托商', 0),
    ('BRAND-SUPERMARKET', '商超演示品牌', '', '', '', '', 1),
    ('BRAND-125', '125演示品牌', '', '', '', '', 0),
    ('BRAND-LONGTEXT', '长文案演示品牌', '', '', '', '', 1),
    ('BRAND-MISSING', '缺资料演示品牌', '', '', '', '', 0);

INSERT INTO CD_BUSINESS_ADDRESS (
    BA_ID, BA_PHONE, BA_FULL_NAME, BA_ADDRESS, BA_PLACE_OF_ORIGIN, BA_PRODUCTION_LICENSE_NO
) VALUES
    ('BA-PHASE2-NORMAL', '400-100-0001', '江苏演示食品有限公司', '江苏省苏州市协同大道 100 号', '江苏 苏州', 'SC202606210001'),
    ('BA-PHASE2-CONSIGNOR', '400-100-0002', '上海演示食品制造有限公司', '上海市松江区生产路 88 号', '上海', 'SC202606210002'),
    ('BA-PHASE2-SUPERMARKET', '400-100-0003', '浙江演示供应链食品有限公司', '浙江省杭州市商超路 18 号', '浙江 杭州', 'SC202606210003'),
    ('BA-PHASE2-125', '400-100-0004', '山东演示速冻食品有限公司', '山东省青岛市冷链路 125 号', '山东 青岛', 'SC202606210004'),
    ('BA-PHASE2-LONGTEXT', '400-100-0005', '广东演示食品科技有限公司', '广东省佛山市样张工业园 9 号', '广东 佛山', 'SC202606210005');

INSERT INTO CD_BUSINESS_PRODUCT_STANDARD (BPS_ID, BPS_CODE, BPS_BA_ID) VALUES
    ('BPS-PHASE2-NORMAL', 'Q/PHASE2 0001S', 'BA-PHASE2-NORMAL'),
    ('BPS-PHASE2-CONSIGNOR', 'Q/PHASE2 0002S', 'BA-PHASE2-CONSIGNOR'),
    ('BPS-PHASE2-SUPERMARKET', 'Q/PHASE2 0003S', 'BA-PHASE2-SUPERMARKET'),
    ('BPS-PHASE2-125', 'Q/PHASE2 0125S', 'BA-PHASE2-125'),
    ('BPS-PHASE2-LONGTEXT', 'Q/PHASE2 0005S', 'BA-PHASE2-LONGTEXT');

INSERT INTO hsh_Name_Conversion_Product_Standard (PS_ID, PS_NC_ID, PS_BPS_ID) VALUES
    ('PS-PHASE2-NORMAL', 'DEMO-BOX-NORMAL', 'BPS-PHASE2-NORMAL'),
    ('PS-PHASE2-CONSIGNOR', 'DEMO-BOX-CONSIGNOR', 'BPS-PHASE2-CONSIGNOR'),
    ('PS-PHASE2-SUPERMARKET', 'DEMO-BOX-SUPERMARKET', 'BPS-PHASE2-SUPERMARKET'),
    ('PS-PHASE2-125', 'DEMO-BOX-125', 'BPS-PHASE2-125'),
    ('PS-PHASE2-LONGTEXT', 'DEMO-BOX-LONGTEXT', 'BPS-PHASE2-LONGTEXT');

INSERT INTO hsh_Name_Conversion (
    id, cInvCode_hc, cInvName_hc, cPackageReportConfig, cBagReportConfig,
    cPackageReportTemplate, cBagReportTemplate, cWatermark, cPackageSize,
    cPackagingLabel, cCustomBrand, cAddMemo, cStoreMode, cShelfLife,
    nFlagPlace, cCustomerBarCode, cGoodsBarCode, cBarCodeType,
    cCompanyTypeName, cIngredientsList, cNutritionFacts, cNetContent,
    cProductClass, cInstructions, cAllergenInfo, cKindReminder, lShelfLifeConvert
) VALUES
    ('DEMO-BOX-NORMAL', 'DMBX001', 'DEMO-BOX 普通箱贴产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '', '1kg x 8袋/箱',
     '普通演示产品箱贴', 'BRAND-NORMAL', '常规渠道', 'FROZEN', '12 months',
     0, '6920260600011', '6920260600011', 151,
     '生产商', '小麦粉、猪肉、饮用水、洋葱、食用盐、酱油、香辛料', '营养成份表：每100g 能量 860kJ，蛋白质 9.0g，脂肪 5.8g，碳水化合物 29.1g，钠 480mg', '净含量：1kg',
     '速冻调制食品', '食用方法：充分加热后食用。', '含小麦、大豆及其制品。', '请于-18℃以下冷冻保存。', 0),
    ('DEMO-BOX-CONSIGNOR', 'DMBX002', 'DEMO-BOX 委托商箱贴产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '委托生产', '800g x 10袋/箱',
     '委托商演示产品箱贴', 'BRAND-CONSIGNOR', '委托商信息必须打印', 'FROZEN', '10 months',
     0, '6920260600028', '6920260600028', 151,
     '受委托生产商', '鸡肉、面粉、饮用水、植物油、食用盐、白砂糖、复合调味料', '营养成份表：每100g 能量 920kJ，蛋白质 10.2g，脂肪 7.1g，碳水化合物 26.4g，钠 510mg', '净含量：800g',
     '速冻面米制品', '食用方法：无需解冻，蒸制或煎制至中心熟透。', '含小麦、蛋、大豆及其制品。', '委托商名称、电话和地址需随箱贴输出。', 0),
    ('DEMO-BOX-SUPERMARKET', 'DMBX003', 'DEMO-BOX 商超专供箱贴产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '商超专供', '500g x 12袋/箱',
     '商超专供演示产品箱贴', 'BRAND-SUPERMARKET', '客户条码优先', 'FROZEN', '9 months',
     1, '6933333300003', '6920260600035', 151,
     '生产商', '猪肉、蔬菜、面粉、饮用水、酱油、食用盐、香辛料', '营养成份表：每100g 能量 880kJ，蛋白质 8.8g，脂肪 6.6g，碳水化合物 27.0g，钠 500mg', '净含量：500g',
     '商超定制速冻食品', '食用方法：请按门店销售标签提示充分加热。', '含小麦、大豆及其制品。', '本品为商超专供版本，条码按客户规则输出。', 0),
    ('DEMO-BOX-125', 'DMBX004', 'DEMO-BOX 125规格箱贴产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '', '125g x 40袋/箱',
     '125规格演示产品箱贴', 'BRAND-125', '125变体', 'FROZEN', '12 months',
     0, '6920260600042', '6920260600042', 151,
     '生产商', '小麦粉、猪肉、饮用水、白菜、食用盐、酱油、香辛料', '营养成份表：每100g 能量 835kJ，蛋白质 8.1g，脂肪 5.5g，碳水化合物 30.0g，钠 460mg', '净含量：125g x 40袋',
     '125规格速冻食品', '食用方法：按包装标识加热至熟透。', '含小麦、大豆及其制品。', '适用于125规格模板验收。', 0),
    ('DEMO-BOX-LONGTEXT', 'DMBX005', 'DEMO-BOX 长文本箱贴产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '长文本验收', '1.5kg x 6袋/箱',
     '长文本演示产品箱贴', 'BRAND-LONGTEXT', '用于测试换行、缩放和字段完整性', 'FROZEN', '15 months',
     0, '6920260600059', '6920260600059', 151,
     '生产商', '配料表：精选小麦粉、猪肉、鸡肉、饮用水、洋葱、胡萝卜、香菇、植物油、食用盐、白砂糖、酱油、蚝油、复合香辛料、酵母抽提物，内容较长用于验证箱贴模板自动换行和字段承载能力。', '营养成份表：每100g 能量 901kJ，蛋白质 9.6g，脂肪 6.4g，碳水化合物 28.8g，钠 530mg；本字段故意保持较长，用于验证预览、渲染和打印样张中的排版稳定性。', '净含量：1.5kg（150g x 10袋）',
     '长文本速冻调制食品', '食用方法：无需解冻，水开后蒸制8至10分钟或煎制至两面金黄并确保中心完全熟透；请根据锅具、火力和产品数量适当调整时间，食用前注意防烫。', '致敏原信息：含小麦、大豆、蛋及其制品；同一生产线亦加工含乳、花生、芝麻、鱼虾类成分的产品，过敏体质消费者请谨慎选择。', '温馨提示：购买后请尽快放入-18℃以下冷冻环境保存，开封后请密封并尽快食用，反复冻融可能影响口感和品质。', 0),
    ('DEMO-BOX-MISSING', 'DMBX006', 'DEMO-BOX 缺失资料箱贴产品', 'BOX-LEGACY', 'BAG-LEGACY',
     'Legacy Box Template', 'Legacy Bag Template', '资料缺失', '1kg x 6袋/箱',
     '缺失资料演示产品箱贴', 'BRAND-MISSING', '故意不维护产品标准关系', 'FROZEN', '12 months',
     0, '6920260600066', '6920260600066', 151,
     '生产商', '小麦粉、饮用水、食用盐', '营养成份表：每100g 能量 780kJ，蛋白质 7.1g，脂肪 4.5g，碳水化合物 31.0g，钠 430mg', '净含量：1kg',
     '诊断演示食品', '食用方法：充分加热后食用。', '含小麦及其制品。', '该样本故意缺少产品标准和企业资质关联，用于诊断提示。', 0);
