# Phase 2.1 Label Printing Productization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split the current demo into a real print workbench and template studio, enrich fake data, and make template editing minimally usable for real box-label layout work.

**Architecture:** Keep the Spring Boot modular monolith. Reuse current `label-printing`, `template-studio`, and `name-conversion` APIs, but separate browser workspaces into different static pages served by MVC routes. Preserve fake H2 tables as SQL Server-shaped legacy stand-ins and keep Java-owned state in `LP_*` tables.

**Tech Stack:** Java 8, Spring Boot 2.7.x, Maven, H2/Flyway fake data, JdbcTemplate, JasperReports, vanilla HTML/CSS/JS, JUnit 5, MockMvc.

---

## File Structure

- Modify `src/main/java/com/example/cx/boxlabel/api/WebPageController.java`: create a small MVC controller for `/`, `/print-workbench`, and `/template-studio`.
- Create `src/main/resources/static/print-workbench.html`: print-only workspace.
- Create `src/main/resources/static/template-studio.html`: template maintenance workspace.
- Keep `src/main/resources/static/index.html` temporarily as a redirect or compatibility shell only if needed.
- Modify `src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java`: assert print workspace has no template editor.
- Modify `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`: assert template studio page and template API behavior.
- Modify `src/main/resources/db/migration/V2__fake_legacy_label_sources.sql`: add realistic fake products.
- Modify `src/main/java/com/example/cx/boxlabel/application/BoxLabelFieldContractService.java`: add field group metadata or expose enough data for grouped preview.
- Modify `src/main/java/com/example/cx/boxlabel/domain/LabelTemplateElement.java`: add optional editor/rendering fields only if required for long text and alignment.
- Modify `src/main/java/com/example/cx/boxlabel/rendering/JasperBoxLabelRenderer.java`: support new element style fields when they are added.
- Modify `README.md` and `docs/label-printing-database.md`: document Phase 2.1 routes and fake data.

## Task 1: Add Route Tests For Separate Workspaces

**Files:**
- Modify: `src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java`
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`
- Later implementation: `src/main/java/com/example/cx/boxlabel/api/WebPageController.java`
- Later implementation: `src/main/resources/static/print-workbench.html`
- Later implementation: `src/main/resources/static/template-studio.html`

- [ ] **Step 1: Write failing test for print workspace route**

In `BoxLabelControllerTest`, replace `rootServesOperationalPrintWorkspace` with:

```java
@Test
void rootAndPrintWorkbenchServePrintOnlyWorkspace() throws Exception {
    mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("print-workbench.html"));

    mockMvc.perform(get("/print-workbench"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("print-workbench.html"));

    String html = new String(
            mockMvc.perform(get("/print-workbench.html"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray(),
            StandardCharsets.UTF_8
    );

    org.assertj.core.api.Assertions.assertThat(html)
            .contains("箱贴打印工作台")
            .contains("function renderPreviewGroups")
            .contains("printJobs")
            .contains("job-template")
            .doesNotContain("templateCanvas")
            .doesNotContain("elementInspector")
            .doesNotContain("function renderTemplateEditor")
            .doesNotContain("旧模板导入")
            .doesNotContain("样张识别");
}
```

- [ ] **Step 2: Write failing test for template studio route**

Add to `TemplateStudioControllerTest`:

```java
@Test
void templateStudioServesDedicatedTemplateMaintenanceWorkspace() throws Exception {
    mockMvc.perform(get("/template-studio"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("template-studio.html"));

    String html = mockMvc.perform(get("/template-studio.html"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    org.assertj.core.api.Assertions.assertThat(html)
            .contains("模板维护中心")
            .contains("templateCanvas")
            .contains("fieldPalette")
            .contains("elementInspector")
            .contains("layerList")
            .contains("sampleProductId")
            .contains("function renderTemplateEditor")
            .contains("function duplicateSelectedElements")
            .contains("function alignSelectedElements")
            .contains("function moveSelectedLayer")
            .doesNotContain("提交打印");
}
```

- [ ] **Step 3: Run tests and verify they fail**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest#rootAndPrintWorkbenchServePrintOnlyWorkspace,TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: FAIL because `WebPageController`, `print-workbench.html`, and `template-studio.html` do not exist yet.

- [ ] **Step 4: Implement minimal route controller**

Create `src/main/java/com/example/cx/boxlabel/api/WebPageController.java`:

```java
package com.example.cx.boxlabel.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String root() {
        return "forward:print-workbench.html";
    }

    @GetMapping("/print-workbench")
    public String printWorkbench() {
        return "forward:print-workbench.html";
    }

    @GetMapping("/template-studio")
    public String templateStudio() {
        return "forward:template-studio.html";
    }
}
```

- [ ] **Step 5: Create temporary static pages from existing page**

Copy the current `src/main/resources/static/index.html` into:

- `src/main/resources/static/print-workbench.html`
- `src/main/resources/static/template-studio.html`

Then edit text markers only:

- In `print-workbench.html`, keep `箱贴打印工作台`.
- In `template-studio.html`, change the page title and header to `模板维护中心`.

Do not remove sections yet; Task 3 and Task 4 will split behavior.

- [ ] **Step 6: Run tests and verify route assertions advance**

Run the same test command. Expected: route assertions pass, but negative assertions fail because copied pages still contain both print and template controls.

- [ ] **Step 7: Commit**

```powershell
git add src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java src/main/java/com/example/cx/boxlabel/api/WebPageController.java src/main/resources/static/print-workbench.html src/main/resources/static/template-studio.html
git commit -m "test: define separate print and template workspaces"
```

## Task 2: Expand Fake Legacy Data

**Files:**
- Modify: `src/test/java/com/example/cx/boxlabel/infrastructure/LegacyBoxLabelQueryRepositoryTest.java`
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`
- Modify: `src/main/resources/db/migration/V2__fake_legacy_label_sources.sql`

- [ ] **Step 1: Write failing repository test for six product scenarios**

In `LegacyBoxLabelQueryRepositoryTest`, add:

```java
@Test
void fakeLegacyDataCoversPhaseTwoBusinessScenarios() {
    List<BoxLabelProductSummary> products = repository.searchProducts(new ProductSearchCriteria());

    org.assertj.core.api.Assertions.assertThat(products)
            .extracting(BoxLabelProductSummary::getProductConfigId)
            .contains(
                    "DEMO-BOX-NORMAL",
                    "DEMO-BOX-CONSIGNOR",
                    "DEMO-BOX-SUPERMARKET",
                    "DEMO-BOX-125",
                    "DEMO-BOX-LONGTEXT",
                    "DEMO-BOX-MISSING"
            );
}
```

If the test class currently uses a different repository field name, use the existing field.

- [ ] **Step 2: Write failing API test for long text and missing diagnostic data**

Add to `TemplateStudioControllerTest`:

```java
@Test
void fakeProductsExposeLongTextAndMissingDataScenarios() throws Exception {
    mockMvc.perform(get("/api/name-conversions/products").param("keyword", "DEMO-BOX"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()", greaterThan(5)))
            .andExpect(jsonPath("$.items[*].productConfigId", hasItem("DEMO-BOX-LONGTEXT")))
            .andExpect(jsonPath("$.items[*].productConfigId", hasItem("DEMO-BOX-MISSING")));

    mockMvc.perform(post("/api/box-labels/preview")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productConfigId\":\"DEMO-BOX-LONGTEXT\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.row.ingredients", containsString("配料表")))
            .andExpect(jsonPath("$.row.nutritionFacts", containsString("能量")))
            .andExpect(jsonPath("$.row.instructions", containsString("食用")));

    mockMvc.perform(get("/api/box-labels/diagnostics/DEMO-BOX-MISSING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.missingRequiredFields.length()", greaterThan(0)));
}
```

- [ ] **Step 3: Run tests and verify failure**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=LegacyBoxLabelQueryRepositoryTest,TemplateStudioControllerTest#fakeProductsExposeLongTextAndMissingDataScenarios" test
```

Expected: FAIL because the six product ids are not seeded.

- [ ] **Step 4: Add fake seed rows**

In `V2__fake_legacy_label_sources.sql`, add rows for the six product ids. Use existing table columns; do not add new columns in this task.

Required values:

- `DEMO-BOX-NORMAL`: complete normal product.
- `DEMO-BOX-CONSIGNOR`: use a brand row with non-empty `BL_CONSIGNOR_NAME`, phone, address, type.
- `DEMO-BOX-SUPERMARKET`: use `cWatermark='商超专供'`, customer barcode different from goods barcode.
- `DEMO-BOX-125`: use package size text containing `125`.
- `DEMO-BOX-LONGTEXT`: make `cIngredientsList`, `cNutritionFacts`, `cInstructions`, `cAllergenInfo`, and `cKindReminder` longer than 80 Chinese characters.
- `DEMO-BOX-MISSING`: leave product standard relationship absent and use a brand/address combination that causes diagnostics to report missing required fields.

- [ ] **Step 5: Adjust diagnostics only if needed**

If `DEMO-BOX-MISSING` does not report missing fields because empty strings are not checked, update `BoxLabelTemplateService.diagnose` to treat blank required fields as missing:

```java
private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
}
```

Use `isBlank` for required fields such as `productStandardNo`, `company`, `address`, `productionLicenseNo`, and `productBarcode`.

- [ ] **Step 6: Run tests and verify pass**

Run the same command. Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/test/java/com/example/cx/boxlabel/infrastructure/LegacyBoxLabelQueryRepositoryTest.java src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java src/main/resources/db/migration/V2__fake_legacy_label_sources.sql src/main/java/com/example/cx/boxlabel/application/BoxLabelTemplateService.java
git commit -m "test: expand fake label data scenarios"
```

## Task 3: Build Print-Only Workbench

**Files:**
- Modify: `src/main/resources/static/print-workbench.html`
- Modify: `src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java`

- [ ] **Step 1: Write failing assertions for grouped preview**

Extend `rootAndPrintWorkbenchServePrintOnlyWorkspace` assertions:

```java
org.assertj.core.api.Assertions.assertThat(html)
        .contains("previewGroups")
        .contains("基础信息")
        .contains("企业信息")
        .contains("法规信息")
        .contains("产品说明")
        .contains("条码二维码")
        .contains("委托商")
        .contains("function renderPreviewGroups");
```

- [ ] **Step 2: Run test and verify failure**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest#rootAndPrintWorkbenchServePrintOnlyWorkspace" test
```

Expected: FAIL because `print-workbench.html` still has old ungrouped preview.

- [ ] **Step 3: Remove template editing markup from print page**

In `print-workbench.html`, delete the right-column template maintenance controls:

- `templateCanvas`
- `fieldPalette`
- `elementInspector`
- `saveTemplateBtn`
- `saveElementsBtn`
- `legacyImportBtn`
- `imageImportBtn`
- element add/delete buttons

Keep template binding controls:

- `bindBox`
- `bindBag`
- `saveBindingBtn`

- [ ] **Step 4: Replace preview fields container**

Use this markup in the preview section:

```html
<div id="previewGroups" class="preview-groups"></div>
```

- [ ] **Step 5: Replace `renderPreview` with grouped renderer**

Add:

```javascript
const PREVIEW_GROUPS = [
    ['基础信息', [
        ['箱贴名称', 'boxLabelName'],
        ['存货名称', 'inventoryName'],
        ['装箱规格', 'packageSpec'],
        ['生产日期', 'productionDate'],
        ['班次', 'shift'],
        ['保质期', 'shelfLife'],
        ['贮存条件', 'storageCondition']
    ]],
    ['品牌与Logo', [
        ['品牌', 'brandName'],
        ['品牌备注', 'addMemo'],
        ['Logo路径', 'logoPath'],
        ['长Logo', 'longLogo'],
        ['Logo尺寸', 'logoSize']
    ]],
    ['企业信息', [
        ['公司', 'company'],
        ['地址', 'address'],
        ['联系方式', 'contact'],
        ['产地', 'origin'],
        ['公司类型', 'companyTypeName']
    ]],
    ['法规信息', [
        ['生产许可证', 'productionLicenseNo'],
        ['产品标准号', 'productStandardNo']
    ]],
    ['产品说明', [
        ['净含量', 'netContent'],
        ['产品类别', 'productClass'],
        ['配料表', 'ingredients'],
        ['营养成份', 'nutritionFacts'],
        ['食用说明', 'instructions'],
        ['致敏原', 'allergenInfo'],
        ['温馨提示', 'reminder']
    ]],
    ['条码二维码', [
        ['产品条码', 'productBarcode'],
        ['原产品条码', 'originalProductBarcode'],
        ['原客户条码', 'originalCustomerBarcode'],
        ['条码类型', 'barcodeType'],
        ['二维码内容', 'qrCode']
    ]],
    ['委托商', [
        ['委托商', 'consignorName'],
        ['委托商电话', 'consignorPhone'],
        ['委托商地址', 'consignorAddress'],
        ['委托类型', 'consignorType']
    ]]
];

function renderPreviewGroups(row) {
    $('previewGroups').innerHTML = PREVIEW_GROUPS.map(([groupName, fields]) => {
        const rows = fields.map(([label, field]) =>
            `<div class="kv"><span>${escapeHtml(label)}</span>${escapeHtml(row[field])}</div>`
        ).join('');
        return `<div class="preview-group"><div class="source-category"><span>${escapeHtml(groupName)}</span><span class="source-count">${fields.length} 项</span></div><div class="kv-grid">${rows}</div></div>`;
    }).join('');
}
```

Update preview action:

```javascript
renderPreviewGroups(data.row);
```

- [ ] **Step 6: Remove template editor JavaScript from print page**

Remove functions only used by template editing:

- `renderFieldPalette`
- `loadTemplateIntoEditor`
- `copyEditorElements`
- `renderTemplateEditor`
- `editorElementClass`
- `styleElementNode`
- `editorElementText`
- `renderElementInspector`
- `elementTypeOptions`
- `fieldOptions`
- `addTemplateElement`
- `beginElementDrag`
- `updateSelectedElement`
- `selectEditorElement`
- `selectedEditorElement`
- `deleteSelectedElement`
- `saveEditorElements`
- `saveElements`

Keep shared functions:

- `api`
- `escapeHtml`
- `renderProducts`
- `loadTemplates`
- `loadBinding`
- `saveBinding`
- `preview`
- `render`
- `printJob`
- `loadJobs`

- [ ] **Step 7: Run test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest#rootAndPrintWorkbenchServePrintOnlyWorkspace" test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```powershell
git add src/main/resources/static/print-workbench.html src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java
git commit -m "feat: split print-only workbench"
```

## Task 4: Build Template Studio Workspace

**Files:**
- Modify: `src/main/resources/static/template-studio.html`
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`

- [ ] **Step 1: Write failing assertions for editor controls**

Extend `templateStudioServesDedicatedTemplateMaintenanceWorkspace`:

```java
org.assertj.core.api.Assertions.assertThat(html)
        .contains("复制元素")
        .contains("左对齐")
        .contains("右对齐")
        .contains("上移一层")
        .contains("置顶")
        .contains("多行文本")
        .contains("textAlign")
        .contains("selectedElementIds");
```

- [ ] **Step 2: Run test and verify failure**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: FAIL because controls do not exist.

- [ ] **Step 3: Remove print-only controls from template studio**

In `template-studio.html`, remove:

- printer name
- copies
- operator
- submit print
- browser print
- print jobs panel

Keep:

- template list
- template form
- field palette
- element buttons
- canvas
- inspector
- import buttons
- preview output panel

- [ ] **Step 4: Add layer list and editor actions**

Add markup:

```html
<div class="actions">
    <button id="duplicateElementBtn">复制元素</button>
    <button id="alignLeftBtn">左对齐</button>
    <button id="alignRightBtn">右对齐</button>
    <button id="alignTopBtn">上对齐</button>
    <button id="alignBottomBtn">下对齐</button>
    <button id="layerUpBtn">上移一层</button>
    <button id="layerDownBtn">下移一层</button>
    <button id="layerTopBtn">置顶</button>
    <button id="layerBottomBtn">置底</button>
</div>
<div id="layerList" class="list"></div>
```

Add sample product selector:

```html
<select id="sampleProductId"></select>
<button id="templatePreviewBtn" class="primary">模板预览</button>
```

- [ ] **Step 5: Add selection state**

In state:

```javascript
selectedElementIds: []
```

Replace single `selectedElementId` usage with helper:

```javascript
function selectedEditorElements() {
    return state.editorElements.filter((item) => state.selectedElementIds.indexOf(item.id) >= 0);
}

function primarySelectedElement() {
    const selected = selectedEditorElements();
    return selected.length ? selected[0] : null;
}
```

- [ ] **Step 6: Implement duplicate**

```javascript
function duplicateSelectedElements() {
    const selected = selectedEditorElements();
    if (!selected.length) return;
    const copies = selected.map((element) => Object.assign({}, element, {
        id: newElementId(),
        sortOrder: state.editorElements.length + 1,
        leftMm: roundMm(element.leftMm + 2),
        topMm: roundMm(element.topMm + 2)
    }));
    state.editorElements = state.editorElements.concat(copies);
    state.selectedElementIds = copies.map((item) => item.id);
    renderTemplateEditor(state.editorTemplate);
}
```

- [ ] **Step 7: Implement alignment**

```javascript
function alignSelectedElements(mode) {
    const selected = selectedEditorElements();
    if (selected.length < 2) return;
    const anchor = selected[0];
    selected.slice(1).forEach((element) => {
        if (mode === 'left') element.leftMm = anchor.leftMm;
        if (mode === 'right') element.leftMm = roundMm(anchor.leftMm + anchor.widthMm - element.widthMm);
        if (mode === 'top') element.topMm = anchor.topMm;
        if (mode === 'bottom') element.topMm = roundMm(anchor.topMm + anchor.heightMm - element.heightMm);
    });
    renderTemplateEditor(state.editorTemplate);
}
```

- [ ] **Step 8: Implement layer move**

```javascript
function moveSelectedLayer(mode) {
    const selectedIds = state.selectedElementIds.slice();
    if (!selectedIds.length) return;
    const sorted = state.editorElements.slice().sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0));
    selectedIds.forEach((id) => {
        const index = sorted.findIndex((item) => item.id === id);
        if (index < 0) return;
        if (mode === 'up' && index < sorted.length - 1) {
            const next = sorted[index + 1];
            sorted[index + 1] = sorted[index];
            sorted[index] = next;
        }
        if (mode === 'down' && index > 0) {
            const prev = sorted[index - 1];
            sorted[index - 1] = sorted[index];
            sorted[index] = prev;
        }
        if (mode === 'top') {
            const item = sorted.splice(index, 1)[0];
            sorted.push(item);
        }
        if (mode === 'bottom') {
            const item = sorted.splice(index, 1)[0];
            sorted.unshift(item);
        }
    });
    state.editorElements = sorted.map((item, index) => Object.assign({}, item, {sortOrder: index + 1}));
    renderTemplateEditor(state.editorTemplate);
}
```

- [ ] **Step 9: Wire buttons**

```javascript
$('duplicateElementBtn').onclick = duplicateSelectedElements;
$('alignLeftBtn').onclick = () => alignSelectedElements('left');
$('alignRightBtn').onclick = () => alignSelectedElements('right');
$('alignTopBtn').onclick = () => alignSelectedElements('top');
$('alignBottomBtn').onclick = () => alignSelectedElements('bottom');
$('layerUpBtn').onclick = () => moveSelectedLayer('up');
$('layerDownBtn').onclick = () => moveSelectedLayer('down');
$('layerTopBtn').onclick = () => moveSelectedLayer('top');
$('layerBottomBtn').onclick = () => moveSelectedLayer('bottom');
```

- [ ] **Step 10: Run test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: PASS.

- [ ] **Step 11: Commit**

```powershell
git add src/main/resources/static/template-studio.html src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java
git commit -m "feat: add dedicated template studio"
```

## Task 5: Add Template Copy Endpoint

**Files:**
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`
- Modify: `src/main/java/com/example/cx/boxlabel/api/TemplateStudioController.java`
- Modify: `src/main/java/com/example/cx/boxlabel/application/BoxLabelTemplateService.java`

- [ ] **Step 1: Write failing API test**

Add to `TemplateStudioControllerTest`:

```java
@Test
void templateCanBeCopiedWithElements() throws Exception {
    mockMvc.perform(post("/api/label-templates/box-config-standard/copy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"code\":\"box-config-copy\",\"name\":\"Box Config Copy\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("box-config-copy"))
            .andExpect(jsonPath("$.name").value("Box Config Copy"))
            .andExpect(jsonPath("$.engine").value("CONFIG_LAYOUT"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.elements.length()", greaterThan(8)))
            .andExpect(jsonPath("$.elements[*].fieldName", hasItem("boxLabelName")));
}
```

- [ ] **Step 2: Create request class**

Create `src/main/java/com/example/cx/boxlabel/domain/LabelTemplateCopyRequest.java`:

```java
package com.example.cx.boxlabel.domain;

public class LabelTemplateCopyRequest {
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

- [ ] **Step 3: Add controller endpoint**

In `TemplateStudioController`:

```java
@PostMapping("/api/label-templates/{templateCode}/copy")
public LabelTemplate copy(@PathVariable String templateCode,
                          @RequestBody LabelTemplateCopyRequest request) {
    return templateService.copyTemplate(templateCode, request);
}
```

- [ ] **Step 4: Implement service copy**

In `BoxLabelTemplateService` add:

```java
public LabelTemplate copyTemplate(String sourceCode, LabelTemplateCopyRequest request) {
    LabelTemplate source = requireTemplate(sourceCode);
    String code = requireText(request.getCode(), "code");
    String name = requireText(request.getName(), "name");
    if (templateRepository.findByCode(code) != null) {
        throw new IllegalArgumentException("Template already exists: " + code);
    }
    LabelTemplate copy = LabelTemplate.configLayout(code, name, source.getLabelType(), source.getPageWidthMm(), source.getPageHeightMm());
    copy.setStatus("DRAFT");
    copy.setImportSource("COPY");
    copy.setElements(copyElementsForTemplate(source.getElements(), code));
    return templateRepository.save(copy);
}

private List<LabelTemplateElement> copyElementsForTemplate(List<LabelTemplateElement> elements, String templateCode) {
    List<LabelTemplateElement> copies = new ArrayList<LabelTemplateElement>();
    int index = 1;
    for (LabelTemplateElement element : elements == null ? Collections.<LabelTemplateElement>emptyList() : elements) {
        LabelTemplateElement copy = element.copy();
        copy.setId(templateCode + "-" + index + "-" + UUID.randomUUID().toString().substring(0, 8));
        copy.setSortOrder(index++);
        copies.add(copy);
    }
    return copies;
}
```

If `requireTemplate`, `requireText`, imports, or `LabelTemplateElement.copy()` do not exist, add the minimal equivalents following existing style.

- [ ] **Step 5: Run test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateCanBeCopiedWithElements" test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java src/main/java/com/example/cx/boxlabel/api/TemplateStudioController.java src/main/java/com/example/cx/boxlabel/application/BoxLabelTemplateService.java src/main/java/com/example/cx/boxlabel/domain/LabelTemplateCopyRequest.java
git commit -m "feat: copy label templates"
```

## Task 6: Add Template Preview Flow In Template Studio

**Files:**
- Modify: `src/main/resources/static/template-studio.html`
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`

- [ ] **Step 1: Write API test for existing preview endpoint**

Add to `TemplateStudioControllerTest`:

```java
@Test
void templatePreviewUsesSelectedProductAndTemplate() throws Exception {
    mockMvc.perform(post("/api/label-templates/box-config-standard/preview")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productConfigId\":\"DEMO-BOX-LONGTEXT\",\"productionDate\":\"2026-06-08\",\"shift\":\"A\",\"labelType\":\"BOX\",\"format\":\"PNG\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.templateCode").value("box-config-standard"))
            .andExpect(jsonPath("$.format").value("PNG"))
            .andExpect(jsonPath("$.previewUrl", startsWith("/api/box-labels/files/")));
}
```

- [ ] **Step 2: Run test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templatePreviewUsesSelectedProductAndTemplate" test
```

Expected: PASS if current endpoint already supports this shape; otherwise FAIL and update controller to delegate to render service.

- [ ] **Step 3: Implement front-end product loading**

In `template-studio.html`:

```javascript
async function loadSampleProducts() {
    const data = await api('/api/name-conversions/products?keyword=');
    $('sampleProductId').innerHTML = data.items.map((item) =>
        `<option value="${escapeHtml(item.productConfigId)}">${escapeHtml(item.inventoryName)} / ${escapeHtml(item.productConfigId)}</option>`
    ).join('');
}
```

- [ ] **Step 4: Implement template preview action**

```javascript
async function previewTemplateWithSample() {
    if (!state.editorTemplate) return;
    const body = {
        productConfigId: $('sampleProductId').value,
        productionDate: $('productionDate').value || '2026-06-08',
        shift: $('shift').value || 'A',
        labelType: state.editorTemplate.labelType,
        format: 'PNG'
    };
    const data = await api('/api/label-templates/' + encodeURIComponent(state.editorTemplate.code) + '/preview', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    $('outputPane').innerHTML = `<img alt="模板预览" src="${escapeHtml(data.previewUrl)}">`;
    setStatus('rightStatus', `${data.templateCode} / ${data.format} / ${data.sizeBytes} bytes`, 'ok');
}
```

- [ ] **Step 5: Wire initialization**

```javascript
$('templatePreviewBtn').onclick = previewTemplateWithSample;
loadTemplates().then(loadSampleProducts);
```

- [ ] **Step 6: Run page test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace,TemplateStudioControllerTest#templatePreviewUsesSelectedProductAndTemplate" test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/resources/static/template-studio.html src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java
git commit -m "feat: preview templates with sample products"
```

## Task 7: Full Verification And Documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/label-printing-database.md`
- Modify: `docs/superpowers/specs/2026-06-20-production-sales-java-platform-design.md` only if Phase 2.1 scope changed while implementing.

- [ ] **Step 1: Update README route section**

Add:

```markdown
## Web Workspaces

- `http://localhost:8088/print-workbench`: print-only box/bag label workbench.
- `http://localhost:8088/template-studio`: template maintenance center.
- `http://localhost:8088/`: forwards to the print workbench.
```

- [ ] **Step 2: Update fake data docs**

In `docs/label-printing-database.md`, add a `Phase 2.1 Fake Data` section listing:

- `DEMO-BOX-NORMAL`
- `DEMO-BOX-CONSIGNOR`
- `DEMO-BOX-SUPERMARKET`
- `DEMO-BOX-125`
- `DEMO-BOX-LONGTEXT`
- `DEMO-BOX-MISSING`

- [ ] **Step 3: Run targeted tests**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest,TemplateStudioControllerTest,LegacyBoxLabelQueryRepositoryTest" test
```

Expected: all pass.

- [ ] **Step 4: Run full tests**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn clean test
```

Expected:

```text
Tests run: 26 or more, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 5: Browser smoke test**

Start app:

```powershell
. ..\tools\use-java8-maven.ps1
mvn spring-boot:run
```

Browser checks:

- Visit `http://localhost:8088/print-workbench`.
- Search `DEMO-BOX`.
- Select `DEMO-BOX-LONGTEXT`.
- Preview grouped fields.
- Render PNG.
- Submit browser print and confirm log row shows product, type, operator, template.
- Visit `http://localhost:8088/template-studio`.
- Select `box-config-standard`.
- Confirm 15 or more elements render.
- Duplicate an element.
- Align two selected elements.
- Move layer.
- Save elements.
- Preview with `DEMO-BOX-LONGTEXT`.
- Confirm no browser console errors.

- [ ] **Step 6: Commit docs**

```powershell
git add README.md docs/label-printing-database.md docs/superpowers/specs/2026-06-20-production-sales-java-platform-design.md
git commit -m "docs: describe phase 2.1 workspaces"
```

- [ ] **Step 7: Push branch**

```powershell
git push
```

Expected: `main -> main` or feature branch pushed successfully.

## Self-Review

- Spec coverage: The plan covers workspace split, fake data expansion, grouped field preview, template editor usability, template copy, sample preview, docs, tests, and browser smoke.
- Placeholder scan: This plan contains no `TBD`, no deferred implementation notes, and each task includes concrete paths and commands.
- Type consistency: Route names, endpoint paths, and function names are consistent across tests and implementation steps.
