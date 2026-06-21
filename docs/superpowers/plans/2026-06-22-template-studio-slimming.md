# Template Studio Slimming Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the template maintenance center lighter by prioritizing the canvas and collapsing searchable field support panels.

**Architecture:** Keep the static HTML page and existing Spring Boot APIs. Implement only page-level HTML/CSS/JavaScript changes plus MockMvc static-page assertions.

**Tech Stack:** Java 8, Spring Boot 2.7.x, MockMvc, Maven, vanilla HTML/CSS/JavaScript.

---

## File Structure

- Modify `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`: add page markers for field search, common fields, support layout, and group-open behavior.
- Modify `src/main/resources/static/template-studio.html`: add a compact support grid, field search input, common field group, default-collapsed groups, and search-driven group opening.

## Task 1: Test Template Studio Slimming Markers

**Files:**
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`

- [ ] **Step 1: Add failing page assertions**

In `templateStudioServesDedicatedTemplateMaintenanceWorkspace`, add:

```java
.contains("fieldSearch")
.contains("fieldPaletteSupport")
.contains("support-grid")
.contains("commonFieldNames")
.contains("function renderFieldSearch")
.contains("function shouldOpenFieldGroup")
.contains("常用字段")
```

- [ ] **Step 2: Run targeted test and verify failure**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: FAIL because the new field search and compact support markers are absent.

## Task 2: Implement Compact Field Support

**Files:**
- Modify: `src/main/resources/static/template-studio.html`

- [ ] **Step 1: Add support layout styles**

Add these styles near the existing field group styles:

```css
.editor-tools { display: grid; gap: 8px; }
.support-grid { display: grid; grid-template-columns: minmax(260px, 1fr) minmax(220px, .8fr); gap: 10px; margin-top: 12px; }
.support-panel h3 { margin: 0 0 8px; font-size: 14px; }
.support-panel .list { max-height: 260px; overflow: auto; }
.field-group:not([open]) { background: #fbfcfd; }
```

Update the media query so `.support-grid` becomes one column under `1180px`.

- [ ] **Step 2: Move support panels below canvas**

Replace the existing field/layer row in the middle section with:

```html
<div class="canvas-shell">
    <div class="template-canvas" id="templateCanvas"></div>
</div>
<div class="support-grid" id="fieldPaletteSupport">
    <div class="support-panel">
        <h3>字段面板</h3>
        <div class="field"><label for="fieldSearch">搜索字段</label><input id="fieldSearch" placeholder="字段名 / 显示名 / 分组"></div>
        <div id="fieldPalette" class="list"></div>
    </div>
    <div class="support-panel">
        <h3>图层</h3>
        <div id="layerList" class="list"></div>
    </div>
</div>
```

- [ ] **Step 3: Add common field and search helpers**

Add:

```javascript
const commonFieldNames = ['boxLabelName', 'inventoryName', 'packageSpec', 'productBarcode', 'qrCode', 'productionDate'];

function fieldSearchTerm() {
    return ($('fieldSearch') && $('fieldSearch').value || '').trim().toLowerCase();
}

function fieldMatchesSearch(item, term) {
    if (!term) return true;
    return [item.fieldName, item.displayName, item.category].some((value) => String(value || '').toLowerCase().indexOf(term) >= 0);
}

function shouldOpenFieldGroup(category, items, term) {
    if (category === '常用字段') return true;
    return !!term && (items || []).some((item) => fieldMatchesSearch(item, term));
}

function renderFieldSearch() {
    renderFieldPalette();
}
```

- [ ] **Step 4: Update field grouping**

Update `fieldContractGroups()` so it returns a `常用字段` group first, followed by normal groups. Do not duplicate field buttons inside the same group. Use `commonFieldNames` to select common fields.

- [ ] **Step 5: Update `renderFieldPalette()`**

Filter each group with `fieldSearchTerm()` and skip groups that have no visible items. Render each group as:

```javascript
return `<details class="field-group" ${shouldOpenFieldGroup(category, visibleItems, term) ? 'open' : ''}>
    <summary>${escapeHtml(category)} / ${visibleItems.length} 项</summary>
    ${rows}
</details>`;
```

Keep the existing field button click behavior.

- [ ] **Step 6: Wire field search handler**

After button handler setup, add:

```javascript
$('fieldSearch').oninput = renderFieldSearch;
```

## Task 3: Verify and Commit

**Files:**
- Read/verify: `src/main/resources/static/template-studio.html`
- Read/verify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`

- [ ] **Step 1: Run targeted template studio test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: PASS.

- [ ] **Step 2: Run focused controller tests**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest,TemplateStudioControllerTest" test
```

Expected: PASS.

- [ ] **Step 3: Run full test suite**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn test
```

Expected: `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

- [ ] **Step 4: Smoke template studio page**

Run:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8088/template-studio' -TimeoutSec 10
Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8088/template-studio.html' -TimeoutSec 10
```

Expected: both return `200`, and the HTML includes `fieldSearch` and `fieldPaletteSupport`.

- [ ] **Step 5: Commit**

Run:

```powershell
git add docs/superpowers/specs/2026-06-22-template-studio-slimming-design.md docs/superpowers/plans/2026-06-22-template-studio-slimming.md src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java src/main/resources/static/template-studio.html
git commit -m "feat: slim template studio field panel"
```

## Self-Review

- Spec coverage: Every requirement maps to compact support layout, search, common field group, default collapsed groups, tests, and smoke verification.
- Placeholder scan: No TBD/TODO placeholders.
- Type consistency: Function and id names match planned assertions: `fieldSearch`, `fieldPaletteSupport`, `support-grid`, `commonFieldNames`, `renderFieldSearch`, and `shouldOpenFieldGroup`.
