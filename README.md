# Java8 箱贴打印中台

这是产销协同 Java 重构的标签打印第一阶段。当前目标不是一次替换全部旧系统，而是先完成箱贴打印闭环：Name Conversion 产品配置读取、模板维护、产品模板绑定、预览、渲染、浏览器打印和打印日志。

运行时不调用 VFP、COM、FRX/FRT、`REPORT FORM` 或旧程序回调。旧数据库表在这一阶段保留为只读数据源、导入来源和诊断依据。

## 当前范围

- 操作型 Web 工作台：http://localhost:8088/
- 打印工作台：http://localhost:8088/print-workbench
- 模板维护中心：http://localhost:8088/template-studio
- 默认使用 H2 fake 数据，保留和 SQL Server/U8 真实表一致的读取边界。
- Java 新表使用 `LP_*` 前缀，保存模板、模板元素、产品模板绑定、打印日志和导入日志。
- 旧表如 `hsh_Name_Conversion`、`Inventory`、`CD_PRINT_TEMPLATE`、`CD_BRAND_LOGO` 等只读保留。
- 内置 Jasper 箱贴模板：`box-standard`。
- 内置可编辑配置箱贴模板：`box-config-standard`，可在线调整文字、字段、Logo、条码、二维码、线条、边框、坐标和字号。
- 产品绑定决定箱贴/袋贴使用的模板；请求未显式传模板时，预览、渲染、打印会按绑定自动选择。
- 打印日志记录产品配置、标签类型、操作人、打印机、份数、模板编码、模板版本和输出文件。

## 运行

```powershell
. ..\tools\use-java8-maven.ps1
mvn clean test
mvn spring-boot:run
```

默认端口是 `8088`。

## Web Workspaces

- `http://localhost:8088/`：转到打印工作台。
- `http://localhost:8088/print-workbench`：箱贴/袋贴打印执行工作区，包含产品查询、数据预览、模板绑定、PDF/PNG 生成、提交打印和打印日志。
- `http://localhost:8088/template-studio`：模板维护中心，包含模板列表、字段面板、元素画布、属性编辑、复制模板、元素复制/对齐/层级调整、旧模板导入、样张识别草稿和样例产品预览。

## 真实数据库切换

默认 profile 使用 H2 方便无法连接数据库时继续开发。后续连接 QA SQL Server 时，复制 `src/main/resources/application-local.example.properties` 为 `application-local.properties`，填入本地连接信息，并使用：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver,local
```

注意：不要提交真实连接串。第一版可以让 Java 新表和旧只读表在同一 SQL Server 连接下验证；如果部署要求物理隔离，再拆成 Java 业务库数据源和 U8/旧表只读数据源。

## 关键 API

```http
GET http://localhost:8088/api/name-conversions/products?keyword=示例
GET http://localhost:8088/api/name-conversions/DEMO-BOX-001
GET http://localhost:8088/api/label-templates?labelType=BOX
GET http://localhost:8088/api/product-template-bindings/DEMO-BOX-001
GET http://localhost:8088/api/box-labels/print-jobs
```

```http
POST http://localhost:8088/api/label-templates
Content-Type: application/json

{
  "code": "box-custom",
  "name": "自定义箱贴",
  "labelType": "BOX",
  "pageWidthMm": 120,
  "pageHeightMm": 80
}
```

```http
PUT http://localhost:8088/api/product-template-bindings/DEMO-BOX-001
Content-Type: application/json

{
  "boxTemplateCode": "box-config-standard",
  "bagTemplateCode": "bag-standard"
}
```

```http
POST http://localhost:8088/api/box-labels/render
Content-Type: application/json

{
  "productConfigId": "DEMO-BOX-001",
  "productionDate": "2026-06-08",
  "shift": "A",
  "labelType": "BOX",
  "format": "PDF"
}
```

更多调试请求见 `http/box-label-debug.http`。

## Phase 1 验收口径

- 产品查询、Name Conversion 详情、字段来源诊断可用。
- 模板表、模板元素表、产品模板绑定表、打印日志表、模板导入日志表可持久化。
- `box-config-standard` 能在线调整元素，并能驱动箱贴预览、PNG/PDF 渲染和打印日志。
- 修改产品模板绑定后，箱贴预览、渲染、打印立即使用新模板。
- 历史打印日志保留当时的模板编码、版本和输出文件。
- 旧表继续保留，不由 Java 修改或删除。

## 当前边界

- 模板维护第一版是配置版式，不是完整拖拽设计器。
- 旧模板导入和样张图片识别导入生成草稿，不承诺一键还原复杂 FRX 版式。
- 物理打印第一阶段走浏览器 PDF 打印；强管控打印机后续再加 Windows 打印工作站服务。
