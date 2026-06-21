# Basic Label Printing Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the existing print workbench and template studio usable for the core label-printing loop with reliable action feedback, guarded buttons, diagnostic summaries, grouped field display, and editable-template-first behavior.

**Architecture:** Keep the current Spring Boot + static HTML shape. Reuse existing APIs and add only frontend behavior plus page-level tests that prove the intended workflow affordances are present. Do not change database schema or introduce new UI framework.

**Tech Stack:** Java 8, Spring Boot 2.7.x, MockMvc, Maven, H2/Flyway, vanilla HTML/CSS/JavaScript.

---

## File Structure

- Modify `src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java`: assert print workbench includes guarded actions, diagnostic summary, collapsible field source groups, label-type-aware template options, and explicit output links.
- Modify `src/main/resources/static/print-workbench.html`: add diagnostic summary markup, collapsible field source groups, output links, local selection guards, action wrappers, label-type-aware template filter options, and auto diagnostics after product selection.
- Modify `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`: assert template studio includes grouped field palette, guarded actions, editable-template preference, and user-facing local validation helpers.
- Modify `src/main/resources/static/template-studio.html`: add grouped field palette, local validation guards, action wrappers, clear status handling, editable-template-first selection, multiline element defaults, and safer copy naming.

## Task 1: Harden Print Workbench Core Flow

**Files:**
- Modify: `src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java`
- Modify: `src/main/resources/static/print-workbench.html`

- [ ] **Step 1: Write failing page assertions**

In `BoxLabelControllerTest.rootAndPrintWorkbenchServePrintOnlyWorkspace`, extend the HTML assertions with these markers:

```java
.contains("diagnosticSummary")
.contains("renderDiagnostics")
.contains("field-source-groups")
.contains("field-source-group")
.contains("details open")
.contains("function shouldExpandFieldGroup")
.contains("function renderTemplateFilterOptions")
.contains("function requireSelectedProduct")
.contains("function runAction")
.contains("function handleActionError")
.contains("class=\"output-link\"")
.contains("$('labelType').onchange = () => runAction('leftStatus', async () => {")
```

- [ ] **Step 2: Run targeted test and verify failure**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest#rootAndPrintWorkbenchServePrintOnlyWorkspace" test
```

Expected: FAIL because `diagnosticSummary`, `runAction`, grouped `<details>`, and output link markers are not in the page yet.

- [ ] **Step 3: Add print workbench structure and styles**

In `print-workbench.html`, add this CSS near the existing source styles:

```css
.summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; margin-bottom: 12px; }
.summary-card { border: 1px solid var(--line); border-radius: 4px; padding: 8px; background: #fbfcfd; }
.summary-card strong { display: block; font-size: 16px; }
.field-source-groups { display: grid; gap: 8px; }
.field-source-group { border: 1px solid var(--line); border-radius: 4px; background: #fff; overflow: hidden; }
.field-source-group summary { cursor: pointer; list-style: none; }
.field-source-group summary::-webkit-details-marker { display: none; }
.output-link { display: inline-flex; align-items: center; margin: 8px 0; color: var(--accent-dark); font-weight: 650; text-decoration: none; }
```

Below the preview actions, before `previewGroups`, add:

```html
<div id="diagnosticSummary" class="summary-grid"></div>
```

- [ ] **Step 4: Add guarded action helpers**

In the print workbench script, after `setStatus`, add:

```javascript
function handleActionError(statusId, error) {
    setStatus(statusId, error && error.message ? error.message : String(error), 'error');
}

async function runAction(statusId, action) {
    try {
        return await action();
    } catch (error) {
        handleActionError(statusId, error);
        return null;
    }
}

function requireSelectedProduct(statusId) {
    if (state.selectedProduct) return true;
    setStatus(statusId, '请先选择产品', 'error');
    return false;
}
```

- [ ] **Step 5: Make template filter label-type-aware**

Replace the template filter setup in `loadTemplates` with:

```javascript
function renderTemplateFilterOptions() {
    const labelType = $('labelType').value || 'BOX';
    const candidates = state.templates.filter((item) => item.labelType === labelType);
    const options = ['<option value="">全部模板</option>'].concat(candidates.map((item) =>
        `<option value="${escapeHtml(item.code)}">${escapeHtml(item.name)} / ${escapeHtml(item.code)}</option>`
    )).join('');
    $('templateFilter').innerHTML = options;
}
```

Then call `renderTemplateFilterOptions()` in `loadTemplates` after `state.templates = data.items || []`.

- [ ] **Step 6: Add automatic diagnostics**

Add:

```javascript
async function loadDiagnostics(productConfigId) {
    const data = await api('/api/box-labels/diagnostics/' + encodeURIComponent(productConfigId));
    renderDiagnostics(data);
    return data;
}

function renderDiagnostics(data) {
    const missingCount = data && data.missingRequiredFields ? data.missingRequiredFields.length : 0;
    const warningCount = data && data.warnings ? data.warnings.length : 0;
    $('diagnosticSummary').innerHTML = `
        <div class="summary-card"><span class="muted">缺失字段</span><strong>${missingCount}</strong></div>
        <div class="summary-card"><span class="muted">模板</span><strong>${data && data.templateAvailable ? '可用' : '缺失'}</strong></div>
        <div class="summary-card"><span class="muted">警告</span><strong>${warningCount}</strong></div>
    `;
}
```

Update `selectProduct` so it runs `await loadDiagnostics(productConfigId);` after field sources are loaded.

- [ ] **Step 7: Collapse field source groups**

Replace `renderFieldSources` with:

```javascript
function shouldExpandFieldGroup(category, items) {
    if (category === '基础字段') return true;
    return (items || []).some((item) => item.fieldStatus === 'MISSING' || item.fieldStatus === 'FALLBACK');
}

function renderFieldSources(items) {
    const groups = groupFieldSources(items);
    $('fieldSources').className = 'field-source-groups';
    $('fieldSources').innerHTML = Object.keys(groups).map((category) => {
        const groupItems = groups[category];
        const rows = groupItems.map((item) => {
            const statusClass = item.fieldStatus === 'MISSING' ? 'status-missing' : (item.fieldStatus === 'FALLBACK' ? 'status-fallback' : 'status-ok');
            return `<div class="source-item">
                <span class="source-display-name">${escapeHtml(item.displayName)}</span>
                <span class="source-field-name">${escapeHtml(item.fieldName)} / ${escapeHtml(item.sourceType)}</span>
                <span class="source-status ${statusClass}">${escapeHtml(item.fieldStatus)}</span>
            </div>`;
        }).join('');
        return `<details class="field-source-group" ${shouldExpandFieldGroup(category, groupItems) ? 'open' : ''}>
            <summary class="source-category"><span>${escapeHtml(category)}</span><span class="source-count">${groupItems.length} 项</span></summary>
            ${rows}
        </details>`;
    }).join('');
}
```

- [ ] **Step 8: Add output links and guards to actions**

In `render(format)`, replace `outputPane` assignment with:

```javascript
$('outputPane').innerHTML = `<a class="output-link" href="${escapeHtml(data.previewUrl)}" target="_blank">打开 ${escapeHtml(data.format)} 输出</a>` + (
    format === 'PDF'
        ? `<iframe src="${escapeHtml(data.previewUrl)}"></iframe>`
        : `<img alt="箱贴预览" src="${escapeHtml(data.previewUrl)}">`
);
```

At the start of `saveBinding`, `preview`, `render`, `printJob`, and `diagnose`, call `requireSelectedProduct` with the relevant status id and return if false.

- [ ] **Step 9: Wrap button handlers**

Replace direct button handlers with:

```javascript
$('searchBtn').onclick = () => runAction('leftStatus', searchProducts);
$('diagnoseBtn').onclick = () => runAction('leftStatus', diagnose);
$('previewBtn').onclick = () => runAction('middleStatus', preview);
$('renderPdfBtn').onclick = () => runAction('rightStatus', () => render('PDF'));
$('renderPngBtn').onclick = () => runAction('rightStatus', () => render('PNG'));
$('printBtn').onclick = () => runAction('rightStatus', printJob);
$('browserPrintBtn').onclick = () => state.lastOutput ? window.open(state.lastOutput.previewUrl, '_blank') : setStatus('rightStatus', '请先生成 PDF 或 PNG', 'error');
$('saveBindingBtn').onclick = () => runAction('rightStatus', saveBinding);
$('labelType').onchange = () => runAction('leftStatus', async () => {
    renderTemplateFilterOptions();
    syncTemplateFromBinding();
    await searchProducts();
});
```

- [ ] **Step 10: Run targeted test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest#rootAndPrintWorkbenchServePrintOnlyWorkspace" test
```

Expected: PASS.

- [ ] **Step 11: Commit print workbench changes**

```powershell
git add src/test/java/com/example/cx/boxlabel/api/BoxLabelControllerTest.java src/main/resources/static/print-workbench.html
git commit -m "feat: harden print workbench loop"
```

## Task 2: Harden Template Studio Core Flow

**Files:**
- Modify: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`
- Modify: `src/main/resources/static/template-studio.html`

- [ ] **Step 1: Write failing page assertions**

In `TemplateStudioControllerTest.templateStudioServesDedicatedTemplateMaintenanceWorkspace`, add:

```java
.contains("fieldContractGroups")
.contains("field-group")
.contains("function preferredEditableTemplateCode")
.contains("function requireEditorTemplate")
.contains("function runAction")
.contains("function handleActionError")
.contains("function makeCopyCode")
.contains("多行文本")
.contains("请选择 CONFIG_LAYOUT 模板")
```

- [ ] **Step 2: Run targeted test and verify failure**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: FAIL because grouped field palette and guard markers are not present yet.

- [ ] **Step 3: Add grouped field palette styles**

In `template-studio.html`, add:

```css
.field-contract-groups { display: grid; gap: 8px; }
.field-group { border: 1px solid var(--line); border-radius: 4px; overflow: hidden; }
.field-group summary { cursor: pointer; padding: 8px 10px; background: #f7f9fa; font-weight: 650; }
.field-group .field-item strong { font-weight: 650; }
.field-group .field-item .muted { color: #7a8490; }
```

- [ ] **Step 4: Add action helpers and template guards**

After `setStatus`, add:

```javascript
function handleActionError(statusId, error) {
    setStatus(statusId, error && error.message ? error.message : String(error), 'error');
}

async function runAction(statusId, action) {
    try {
        return await action();
    } catch (error) {
        handleActionError(statusId, error);
        return null;
    }
}

function requireEditorTemplate(statusId) {
    if (!state.editorTemplate) {
        setStatus(statusId, '请选择模板', 'error');
        return false;
    }
    if (state.editorTemplate.engine !== 'CONFIG_LAYOUT') {
        setStatus(statusId, '请选择 CONFIG_LAYOUT 模板', 'error');
        return false;
    }
    return true;
}
```

- [ ] **Step 5: Group field contract display**

Add:

```javascript
function fieldContractGroups() {
    return state.fieldContract.reduce((groups, item) => {
        const category = item.category || '未分组';
        if (!groups[category]) groups[category] = [];
        groups[category].push(item);
        return groups;
    }, {});
}
```

Replace `renderFieldPalette` with grouped `<details>` output using `field-contract-groups` and `field-group`.

- [ ] **Step 6: Prefer editable templates on load**

Add:

```javascript
function preferredEditableTemplateCode() {
    const editable = state.templates.find((item) => item.engine === 'CONFIG_LAYOUT');
    return editable ? editable.code : (state.templates[0] ? state.templates[0].code : null);
}
```

In `loadTemplates`, replace `loadTemplateIntoEditor(state.templates[0].code);` with:

```javascript
const preferredCode = preferredEditableTemplateCode();
if (!state.editorTemplate && preferredCode) {
    loadTemplateIntoEditor(preferredCode);
}
```

- [ ] **Step 7: Improve element creation and copy names**

In `addTemplateElement`, return an error status if `requireEditorTemplate('middleStatus')` is false. For `addMultilineBtn`, call:

```javascript
addTemplateElement('STATIC_TEXT', '', {text: '多行文本', heightMm: 16, widthMm: 60});
```

Add:

```javascript
function makeCopyCode(code) {
    return (code || 'template') + '-copy-' + Date.now().toString().slice(-6);
}
```

Use `makeCopyCode($('templateCode').value)` in `copySelectedTemplate`.

- [ ] **Step 8: Guard save/copy/preview actions**

At the start of `saveElements`, `copySelectedTemplate`, and `previewTemplateWithSample`, call `requireEditorTemplate` with the correct status id and return if false. In `previewTemplateWithSample`, also check `sampleProductId` and show `请选择样例产品` if it is blank.

- [ ] **Step 9: Wrap template studio handlers**

Replace direct handlers with:

```javascript
$('reloadTemplatesBtn').onclick = () => runAction('leftStatus', loadTemplates);
$('labelType').onchange = () => runAction('leftStatus', async () => {
    state.editorTemplate = null;
    await loadTemplates();
});
$('templateStatus').onchange = () => runAction('leftStatus', loadTemplates);
$('saveTemplateBtn').onclick = () => runAction('leftStatus', saveTemplate);
$('copyTemplateBtn').onclick = () => runAction('leftStatus', copySelectedTemplate);
$('saveElementsBtn').onclick = () => runAction('middleStatus', saveElements);
$('legacyImportBtn').onclick = () => runAction('leftStatus', importLegacyTemplate);
$('imageImportBtn').onclick = () => runAction('leftStatus', importImageTemplate);
$('templatePreviewBtn').onclick = () => runAction('rightStatus', previewTemplateWithSample);
```

Wrap add/delete/align/layer buttons with local guards where they can fail.

- [ ] **Step 10: Run targeted test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: PASS.

- [ ] **Step 11: Commit template studio changes**

```powershell
git add src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java src/main/resources/static/template-studio.html
git commit -m "feat: harden template studio loop"
```

## Task 3: Verify Basic Loop

**Files:**
- Read: `src/main/resources/static/print-workbench.html`
- Read: `src/main/resources/static/template-studio.html`

- [ ] **Step 1: Run focused tests**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn "-Dtest=BoxLabelControllerTest,TemplateStudioControllerTest" test
```

Expected: all tests pass.

- [ ] **Step 2: Run full test suite**

Run:

```powershell
. ..\tools\use-java8-maven.ps1
mvn test
```

Expected:

```text
Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 3: Smoke running service**

If `http://localhost:8088` is already running, use it. Otherwise start:

```powershell
. ..\tools\use-java8-maven.ps1
mvn spring-boot:run
```

Smoke with HTTP commands:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8088/print-workbench' -TimeoutSec 10
Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8088/template-studio' -TimeoutSec 10
```

Expected: both return `200`.

- [ ] **Step 4: Smoke render and print APIs**

Run:

```powershell
$body = @{ productConfigId='DEMO-BOX-001'; productionDate='2026-06-22'; shift='A'; labelType='BOX'; format='PNG' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8088/api/box-labels/render' -ContentType 'application/json; charset=utf-8' -Body $body

$printBody = @{ productConfigId='DEMO-BOX-001'; productionDate='2026-06-22'; shift='A'; labelType='BOX'; format='PDF'; printerName='Browser'; copies=1; operator='codex' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8088/api/box-labels/print' -ContentType 'application/json; charset=utf-8' -Body $printBody
```

Expected: render returns `success: true`; print returns `status: BROWSER_PRINT_READY`.

- [ ] **Step 5: Report status**

Run:

```powershell
git status --short --branch
git log --oneline -3
```

Expected: local branch contains the plan commit and the two feature commits. Working tree is clean unless verification generated ignored output.

## Self-Review

- Spec coverage: The plan covers guarded print workbench actions, diagnostic summaries, grouped/collapsible field sources, template studio grouped field palette, editable-template-first selection, save/copy/preview guards, and verification.
- Placeholder scan: No unfinished placeholder instructions remain.
- Type consistency: Function names in tests match implementation snippets: `runAction`, `handleActionError`, `renderDiagnostics`, `renderTemplateFilterOptions`, `fieldContractGroups`, `preferredEditableTemplateCode`, `requireEditorTemplate`, and `makeCopyCode`.
