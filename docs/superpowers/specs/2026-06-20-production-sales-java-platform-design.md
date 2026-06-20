# 产销协同 Java 平台与标签打印中台总体设计

## 1. 设计目标

本设计面向产销协同系统的 Java 重构。总体路线不是一次性重写全部 VFP 系统，而是采用模块化单体和渐进替换策略，先把标签打印中台做成稳定产品能力，再逐步迁入主数据、订单、出库、仓储、权限、配置和审计。

第一优先级是箱贴打印，但箱贴不能作为孤立工具建设。箱贴打印依赖 Name Conversion 产品标签配置、模板维护、品牌 Logo、业务地址、产品标准、储存方式、U8 只读数据、打印日志和诊断能力。因此第一阶段之后的重点应从“能打印”升级为“标签打印中台产品化”。

运行时原则：

- Java 新系统不调用 VFP、COM、FRX/FRT、`REPORT FORM` 或旧程序回调。
- 旧数据库表保留，不由 Java 修改或删除。
- 第一版允许使用 fake H2 数据开发，但 fake 数据结构必须模拟未来真实 SQL Server 表关系。
- Java 新表作为模板、模板元素、绑定、日志和导入记录的主数据。
- 真实 SQL Server 接入时，不改变前端主流程和 API 形态。

## 2. 产品定位

产销协同 Java 平台是面向生产、销售、仓储和打印场景的内部运营系统。标签打印中台是第一个落地产品域。

产品分为四层：

1. 平台底座：用户、权限、配置、审计、文件、诊断。
2. 数据中心：Name Conversion、品牌 Logo、公司地址、产品标准、储存方式、U8 只读数据。
3. 打印中台：模板维护、模板绑定、标签预览、渲染、打印、日志。
4. 业务协同：订单、出库、仓储、生产计划、后续 U8/外部系统集成。

## 3. 用户角色

### 打印员

负责日常箱贴和袋贴打印。只需要查产品、选日期班次、预览、生成 PDF/PNG、打印、查看最近打印日志。打印员不应编辑模板。

### 模板维护员

负责创建和维护箱贴、袋贴、商超、委托商、`125` 等模板。需要字段面板、画布、元素属性、图层、版本、草稿、发布、预览和导入能力。

### 资料维护员

负责维护品牌 Logo、公司地址、产品标准、储存方式、业务主体等主数据。后续这些数据从旧表逐步迁入 Java 主数据。

### 管理员/IT

负责数据库连接、文件目录、模板目录、日志保留、打印机策略、权限分配、系统诊断和上线切换。

## 4. 产品模块地图

```text
产销协同 Java 平台
├─ identity-permission
│  ├─ 用户
│  ├─ 角色
│  ├─ 菜单权限
│  └─ 按钮/功能权限
├─ configuration
│  ├─ 数据库连接参数引用
│  ├─ 输出目录
│  ├─ Logo/模板目录
│  ├─ 打印机策略
│  └─ 日志保留策略
├─ integration-u8
│  ├─ U8 只读查询
│  ├─ 旧业务库只读查询
│  └─ 跨库字段来源诊断
├─ name-conversion
│  ├─ 产品标签配置查询
│  ├─ 产品配置详情
│  ├─ 字段来源诊断
│  └─ 产品模板绑定入口
├─ master-data
│  ├─ 品牌 Logo
│  ├─ 公司地址
│  ├─ 产品标准
│  └─ 储存方式
├─ template-studio
│  ├─ 模板列表
│  ├─ 模板元素
│  ├─ 草稿/发布/版本
│  ├─ 原项目导入
│  ├─ 样张图片识别导入
│  └─ 模板诊断
├─ label-printing
│  ├─ 箱贴打印工作台
│  ├─ 袋贴打印工作台
│  ├─ 数据组装
│  ├─ PDF/PNG 渲染
│  ├─ 浏览器打印
│  └─ 打印日志
└─ audit-log
   ├─ 打印日志
   ├─ 模板变更日志
   ├─ 导入日志
   └─ 错误诊断日志
```

## 5. 页面与工作区规划

### 打印工作台

打印工作台面向打印员，页面要简洁、稳定、高效。

核心能力：

- 产品查询。
- Name Conversion 简要信息。
- 箱贴/袋贴模板绑定展示。
- 生产日期、班次、份数、打印机、操作员。
- 预览数据。
- 生成 PDF/PNG。
- 浏览器打印。
- 最近打印日志。
- 缺模板、缺 Logo、缺字段、条码异常提示。

边界：

- 不编辑模板。
- 不维护 Logo、地址、产品标准。
- 可以跳转到模板维护中心或 Name Conversion 详情。

建议路径：

- `/print-workbench`
- `/box-labels/print`
- `/bag-labels/print`

### 模板维护中心

模板维护中心面向模板维护员，应独立于打印工作台。

核心能力：

- 模板列表和类型筛选：BOX、BAG、CUSTOMER、125 等。
- 模板基础信息：编码、名称、类型、纸张、状态、版本、来源。
- 元素编辑：固定文本、字段文本、长文本、Logo、条码、二维码、线条、矩形、图片。
- 支持多个元素绑定同一个字段。
- 支持多选、复制、删除、拖拽、微调、对齐、图层顺序。
- 支持字体大小、加粗、对齐、边框、换行、溢出策略。
- 支持字段分组：基础、品牌/Logo、生产企业、法规、条码二维码、配料营养、委托商。
- 支持使用 fake 产品或真实产品做实时预览。
- 支持保存草稿、发布版本、复制模板、停用模板。
- 支持旧模板元数据导入和样张图片识别导入，导入结果只能作为草稿。

建议路径：

- `/template-studio`
- `/template-studio/templates/{templateCode}`

### Name Conversion 配置中心

核心能力：

- 查询产品标签配置。
- 查看字段来源和 fallback。
- 查看箱贴模板、袋贴模板、Logo、公司、地址、标准号来源。
- 修改产品模板绑定。
- 后续支持从旧 `hsh_Name_Conversion` 初始化 Java 绑定。

建议路径：

- `/name-conversions`
- `/name-conversions/{productConfigId}`

## 6. 数据策略

### 旧表保留

旧表作为只读数据源、导入来源和诊断依据：

- `UFDATA_001_2018.dbo.hsh_Name_Conversion`
- `UFDATA_001_2018.dbo.Inventory`
- `CD_PRINT_TEMPLATE`
- `CD_BRAND_LOGO`
- `CD_BUSINESS_ADDRESS`
- `CD_BUSINESS_PRODUCT_STANDARD`
- `CD_STORE_MODE`
- `hsh_Name_Conversion_Product_Standard`

Java 不直接修改这些表。旧表字段变更、缺字段、空值、跨库连接问题都通过诊断暴露。

### Java 新表

当前 `LP_*` 表继续作为标签打印中台主数据：

- `LP_LABEL_TEMPLATE`
- `LP_LABEL_TEMPLATE_ELEMENT`
- `LP_PRODUCT_TEMPLATE_BINDING`
- `LP_PRINT_JOB`
- `LP_TEMPLATE_IMPORT_LOG`

后续应扩展：

- `LP_LABEL_TEMPLATE_VERSION`：模板版本快照。
- `LP_LABEL_TEMPLATE_DRAFT`：模板草稿。
- `LP_TEMPLATE_CHANGE_LOG`：模板变更日志。
- `LP_PRINT_FIELD_SNAPSHOT`：打印时字段快照，用于历史追溯。
- `LP_LABEL_FIELD_DICTIONARY`：字段字典，支持字段分组、显示名、类型、是否长文本、是否必填。

### fake 数据要求

fake 数据不是随便造，而是测试真实业务变体。

至少准备 6 类产品：

- 普通箱贴。
- 委托商箱贴。
- 商超/客户定制箱贴。
- `125` 或特殊尺寸箱贴。
- 长配料和长营养成份箱贴。
- 缺 Logo、缺产品标准或缺地址的异常产品。

每类 fake 数据都要覆盖箱贴名、存货名、规格、品牌、Logo、公司、地址、联系方式、产地、生产许可证、标准号、配料表、营养成份、净含量、产品类别、食用说明、致敏原、温馨提示、条码、二维码、委托商信息。

## 7. 模板模型

模板不等同于 Jasper 文件。Java 模板维护中心的核心是配置版式模型。

模板头：

- 模板编码。
- 模板名称。
- 标签类型。
- 纸张宽高。
- 渲染引擎：`CONFIG_LAYOUT`、`JASPER`。
- 状态：草稿、启用、停用、归档。
- 当前版本。
- 来源：内置、手工、新建、旧模板导入、图片识别导入。

模板元素：

- 元素 id。
- 元素类型。
- 字段名或固定文本。
- 坐标和宽高。
- 字号、加粗、对齐、换行。
- 图层顺序。
- 边框和线条属性。
- 条码/二维码配置。
- 长文本溢出策略。

版本策略：

- 编辑模板先保存草稿。
- 发布后生成不可变版本。
- 打印日志记录当时模板编码和版本。
- 历史打印记录不受新版本影响。

## 8. 渲染与打印策略

第一阶段继续使用 JasperReports 输出 PDF/PNG。`CONFIG_LAYOUT` 模板在运行时转换为 JasperDesign，然后生成 PDF/PNG。

条码和二维码：

- QR 使用 ZXing。
- Code128 使用 Barcode4J 或当前 Java 条码服务。
- 验收不只看图片生成，还要验证扫码可读。

物理打印：

- 当前阶段走浏览器 PDF 打印。
- 需要强控打印机时，增加 Windows 打印工作站服务。
- 打印工作站只负责接收已渲染 PDF/PNG 和打印策略，不负责业务数据组装。

## 9. 技术架构

第一阶段继续 Java 8、Spring Boot 2.7.x、Maven。后续单独规划 Java 21 升级。

架构形态采用模块化单体：

- 单一部署单元。
- 内部按包或 Maven module 划分边界。
- API 按业务域拆分 controller/service/repository。
- 数据访问使用 `JdbcTemplate` 或 MyBatis，复杂旧 SQL 不使用 JPA 隐藏。
- 迁移脚本使用 Flyway。
- 文件输出使用配置目录，禁止写死生产路径。

数据源策略：

- 本地默认 H2 fake 数据。
- QA 使用 SQL Server profile。
- 初期可单连接验证旧只读表和 Java 新表。
- 部署需要隔离时拆成 Java 业务库数据源和旧库/U8 只读数据源。

## 10. API 规划

### Name Conversion

- `GET /api/name-conversions/products`
- `GET /api/name-conversions/{productConfigId}`
- `GET /api/name-conversions/{productConfigId}/field-sources`

### 模板维护

- `GET /api/label-templates`
- `POST /api/label-templates`
- `GET /api/label-templates/{templateCode}`
- `PUT /api/label-templates/{templateCode}/elements`
- `POST /api/label-templates/{templateCode}/copy`
- `POST /api/label-templates/{templateCode}/draft`
- `POST /api/label-templates/{templateCode}/publish`
- `POST /api/label-templates/{templateCode}/preview`

### 模板导入

- `POST /api/template-imports/from-legacy`
- `POST /api/template-imports/from-image`
- `GET /api/template-imports`

### 产品模板绑定

- `GET /api/product-template-bindings/{productConfigId}`
- `PUT /api/product-template-bindings/{productConfigId}`

### 标签打印

- `POST /api/box-labels/preview`
- `POST /api/box-labels/render`
- `POST /api/box-labels/print`
- `GET /api/box-labels/print-jobs`
- `GET /api/box-labels/diagnostics/{productConfigId}`

后续袋贴优先复用同一套接口，可通过 `labelType=BAG` 区分，也可以补充 `/api/bag-labels/*` 作为语义入口。

## 11. 阶段计划

### Phase 1：箱贴打印闭环

状态：本地 fake 数据已完成。

范围：

- 产品查询。
- Name Conversion 详情。
- 字段来源诊断。
- Java 新表。
- 模板绑定。
- PDF/PNG 渲染。
- 浏览器打印。
- 打印日志。

### Phase 2：标签打印中台产品化

目标：从 demo 升级为可持续使用的产品模块。

Phase 2.1：

- 拆分打印工作台和模板维护中心。
- 扩充 fake 数据。
- 扩充预览字段。
- 增强模板编辑器基础能力。
- 模板维护中心支持用样品实时预览。

Phase 2.2：

- 模板草稿、发布和版本。
- 模板复制。
- 图层和字段字典。
- 打印字段快照。
- 模板诊断。

### Phase 3：Name Conversion 配置中心

目标：

- 产品字段来源完整展示。
- 产品箱贴/袋贴模板绑定完善。
- 支持从旧 `hsh_Name_Conversion` 初始化 Java 绑定。
- 支持缺字段、缺 Logo、缺标准号诊断。

### Phase 4：袋贴扩展

目标：

- 袋贴复用标签打印中台。
- 复用模板维护、绑定、渲染、打印日志。
- 建立袋贴验收样张。

### Phase 5：模板导入能力

目标：

- 旧 `CD_PRINT_TEMPLATE` 元数据导入。
- FRX 字段清单提取作为参考，不承诺一键还原复杂版式。
- 样张图片识别导入为草稿。
- 用户校正后才能发布。

### Phase 6：主数据迁入

目标：

- 品牌 Logo。
- 公司地址。
- 产品标准。
- 储存方式。
- 与旧表做差异诊断和同步策略。

### Phase 7：真实 SQL Server QA 接入

目标：

- 连接 QA SQL Server。
- 验证跨库读取。
- 选择真实产品样张。
- 与旧 VFP 输出对比尺寸、字体、Logo、条码、二维码。

### Phase 8：打印工作站

目标：

- Windows 打印工作站服务。
- 指定打印机。
- 打印队列。
- 打印失败重试和诊断。

### Phase 9：更大范围产销协同迁移

目标：

- 权限。
- 配置。
- 订单。
- 出库。
- 仓储。
- U8 集成。
- COM 兼容接口逐步替换。

## 12. Phase 2.1 详细范围

Phase 2.1 是下一步建议实施范围。

### 页面拆分

现有 `/` 页面拆成：

- `/print-workbench`
- `/template-studio`

`/` 可以重定向到 `/print-workbench`。

### 打印工作台

保留：

- 产品查询。
- 日期班次。
- 模板绑定展示。
- 预览。
- PDF/PNG。
- 打印。
- 日志。

移除：

- 模板元素编辑。
- 模板导入按钮。
- 模板画布编辑。

### 模板维护中心

新增：

- 模板列表。
- 画布编辑。
- 字段分组面板。
- 元素属性面板。
- 图层列表。
- 样品产品选择。
- 实时预览。

### 编辑器最低可用能力

- 多个相同字段元素。
- 固定文本。
- 字段文本。
- 长文本。
- Logo。
- 条码。
- 二维码。
- 线条。
- 矩形。
- 复制元素。
- 删除元素。
- 多选。
- 对齐：左、右、上、下、水平居中、垂直居中。
- 图层：上移、下移、置顶、置底。
- 键盘微调。
- 坐标和尺寸数字输入。
- 字号、加粗、文本对齐、自动换行。

### fake 数据扩充

至少增加：

- `DEMO-BOX-NORMAL`
- `DEMO-BOX-CONSIGNOR`
- `DEMO-BOX-SUPERMARKET`
- `DEMO-BOX-125`
- `DEMO-BOX-LONGTEXT`
- `DEMO-BOX-MISSING`

## 13. 验收标准

### 产品验收

- 打印员可以在不进入模板维护的情况下完成箱贴打印。
- 模板维护员可以独立维护模板，不影响打印员主流程。
- 修改产品模板绑定后，打印立即使用新模板。
- 修改模板草稿不影响已发布模板。
- 发布新版本后，后续打印使用新版本。
- 历史打印日志仍能看到旧版本信息。

### 技术验收

- `mvn clean test` 通过。
- fake 数据覆盖主要业务变体。
- API 不依赖 H2 专有行为。
- 旧表只读，不产生写入 SQL。
- 渲染 PDF/PNG 成功。
- 打印日志记录完整追溯字段。

### 打印验收

- 至少 3 个真实样张对比旧系统。
- 覆盖普通箱贴、委托商、商超或 `125`。
- 检查尺寸、字体、换行、Logo、条码、二维码。

## 14. 风险与控制

### 旧字段不完整

风险：模板需要的字段没有进入 Java DTO。

控制：字段字典、字段来源诊断、打印前模板字段校验。

### 模板编辑器失控

风险：直接做完整拖拽设计器导致周期过长。

控制：先做配置版式的最低可用能力，复杂在线设计能力分阶段做。

### fake 数据失真

风险：本地通过，真实库失败。

控制：fake 表结构贴近真实表，QA 接入前做 SQL 字段合同测试。

### 打印输出不一致

风险：字体、DPI、条码缩放和旧 VFP 不一致。

控制：PDF/PNG 样张验收，扫码测试，打印站单独验收。

### 旧系统并行期混乱

风险：旧 VFP 和 Java 模板/Logo/地址来源不一致。

控制：明确 Java 主数据范围；旧表只读；导入结果先草稿；上线前冻结样张。

## 15. 决策记录

- 决定采用 Java 模块化单体先行。
- 决定箱贴打印优先，但按标签打印中台建设。
- 决定打印工作台和模板维护中心分离。
- 决定旧表保留只读，Java 新表保存模板、绑定和日志。
- 决定第一阶段使用 fake 数据，但保持真实数据库连接边界。
- 决定模板维护先做配置版式，不做完整 FRX 在线还原。
- 决定浏览器 PDF 打印先行，打印工作站后置。

## 16. 下一步

下一步进入 Phase 2.1 实施计划：

1. 拆分页面。
2. 扩 fake 数据。
3. 扩字段预览。
4. 增强模板编辑器最低可用能力。
5. 增加模板维护中心实时预览。
6. 补充测试和浏览器烟测。
