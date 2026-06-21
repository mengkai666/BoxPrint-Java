# Basic Label Printing Loop Design

## Summary

下一步先完成标签打印中台的基本可用闭环，而不是继续堆字段或重做视觉设计。目标是让一个测试用户可以稳定完成两条主流程：

1. 在打印工作台查询产品、预览标签数据、生成 PDF/PNG、提交打印日志。
2. 在模板维护中心选择模板、编辑元素、保存元素、复制模板、用样例产品预览模板。

字段展示太多的问题本轮只做最小收束：保留诊断能力，但把非当前动作必需的信息折叠或移到次级区域。完整的信息架构和视觉瘦身放到下一轮。

## Chosen Approach

采用“先闭环，后瘦身”的路径。

- 不做整站视觉重构。
- 不新增复杂拖拽设计器能力。
- 不扩展真实 SQL Server 写入或物理打印工作站。
- 重点补齐动作可达性、错误反馈、保存后状态同步、预览结果可见、打印日志可追踪。

这比先优化页面观感更适合当前阶段，因为现有页面已经有大部分 API 和静态页面骨架，缺的是把主动作串稳。

## Print Workbench Scope

打印工作台应支持以下基础流程：

1. 进入 `/print-workbench` 后自动加载模板、产品列表和打印日志。
2. 用户可以按关键字、标签类型、模板筛选产品。
3. 选择产品后自动加载模板绑定、字段来源和诊断摘要。
4. 用户可以修改箱贴/袋贴模板绑定并保存。
5. 用户可以按当前产品、日期、班次、类型和模板生成预览数据。
6. 用户可以生成 PDF 或 PNG，并在结果区看到预览链接。
7. 用户可以提交打印任务，任务写入打印日志。
8. 打印日志刷新后显示产品、类型、操作员、打印机、份数、模板和状态。

字段来源面板只显示摘要和按组折叠内容。默认展开“缺失/异常”和“基础字段”，其余组默认折叠，避免首屏被字段列表吞掉。

## Template Studio Scope

模板维护中心应支持以下基础流程：

1. 进入 `/template-studio` 后加载模板列表、字段契约和样例产品。
2. 用户选择模板后，画布显示元素，右侧显示当前元素属性。
3. 用户可以新增固定文本、字段文本、Logo、条码、二维码、线条和矩形元素。
4. 用户可以选中元素、移动、修改坐标尺寸、字体、加粗、文本和字段来源。
5. 用户可以复制元素、对齐元素、调整层级。
6. 用户可以保存模板基础信息。
7. 用户可以保存模板元素，保存后版本号或状态提示刷新。
8. 用户可以复制模板为草稿。
9. 用户可以选择样例产品并生成模板预览 PNG。

字段面板保留，但默认按业务组展示；字段英文名作为辅助信息弱化显示，不再作为主视觉内容。

## Error Handling

两个页面的 API 调用都要有统一错误处理：

- 按钮动作失败时在当前页面状态区显示错误文本。
- 页面初始化失败时显示可读错误，不让用户面对空白页面。
- 保存、渲染、打印动作成功后显示明确的成功状态。
- 如果没有选择产品或模板，按钮动作应给出本地提示，不发无效请求。

## Data And API Contracts

本轮优先复用已有接口：

- `GET /api/name-conversions/products`
- `GET /api/name-conversions/{id}`
- `GET /api/box-labels/field-contract`
- `GET /api/box-labels/diagnostics/{productConfigId}`
- `POST /api/box-labels/preview`
- `POST /api/box-labels/render`
- `POST /api/box-labels/print`
- `GET /api/box-labels/print-jobs`
- `GET /api/label-templates`
- `POST /api/label-templates`
- `PUT /api/label-templates/{templateCode}/elements`
- `POST /api/label-templates/{templateCode}/copy`
- `POST /api/label-templates/{templateCode}/preview`
- `GET /api/product-template-bindings/{productConfigId}`
- `PUT /api/product-template-bindings/{productConfigId}`

如果前端闭环发现某个接口返回缺少必要字段，可以做小范围 API 补充；不改变现有路径和语义。

## Testing

实现计划应覆盖以下验证：

- MockMvc 测试打印工作台页面包含主流程入口，不包含模板编辑器。
- MockMvc 测试模板中心页面包含模板编辑、保存、复制、样例预览入口，不包含打印提交入口。
- API 测试覆盖模板保存、元素保存、模板复制、样例产品预览、渲染输出和打印日志。
- 全量 `mvn test` 通过。
- 本地服务启动后，手动烟测 `/print-workbench` 和 `/template-studio` 两条流程。

## Out Of Scope

本轮不处理：

- 完整视觉重设计。
- 拖拽设计器高级能力，如吸附线、缩放、多页面模板。
- 真实 SQL Server 写入迁移。
- Windows 物理打印工作站。
- 用户、权限、审计后台。
- 自动把 VFP FRX/FRT 完整转换成 Java 模板。

