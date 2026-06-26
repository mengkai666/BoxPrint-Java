# VFP Report Editor Replacement Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the first working replacement for the VFP report editor at `/template-studio`: a Vue visual editor with drag/resize, field binding, conditional visibility, backend persistence, and backend render support.

**Architecture:** Keep the Java 8 Spring Boot backend and static resource delivery. Add condition fields to the existing `LabelTemplateElement` model and database table, evaluate conditions in the renderer, and replace the `/template-studio` route with a Vue-powered static page that edits the existing template APIs. The current legacy `template-studio.html` remains on disk as a fallback page while `/template-studio` forwards to the new Vue page.

**Tech Stack:** Java 8, Spring Boot 2.7, Flyway, H2/SQL Server-compatible migrations, vanilla static assets, Vue 3 global build from CDN.

---

### Task 1: Persist Element Conditional Visibility

**Files:**
- Modify: `src/main/java/com/example/cx/boxlabel/domain/LabelTemplateElement.java`
- Modify: `src/main/java/com/example/cx/boxlabel/infrastructure/JdbcLabelTemplateRepository.java`
- Modify: `src/main/resources/db/migration/V1__label_printing_schema.sql`
- Create: `src/main/resources/db/migration/V5__label_template_element_visibility_condition.sql`
- Test: `src/test/java/com/example/cx/boxlabel/infrastructure/LabelPrintingPersistenceTest.java`

- [ ] **Step 1: Add a failing persistence assertion**

Add a test that saves an element with:

```json
{
  "visibleWhenField": "brandName",
  "visibleWhenOperator": "equals",
  "visibleWhenValue": "QINRE"
}
```

Expected failure before implementation: returned element has empty condition fields or insert fails because columns do not exist.

- [ ] **Step 2: Run focused persistence test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=LabelPrintingPersistenceTest" test
```

Expected: FAIL on missing condition persistence.

- [ ] **Step 3: Implement model and repository mapping**

Add fields and getters/setters:

```java
private String visibleWhenField;
private String visibleWhenOperator;
private String visibleWhenValue;
```

Map the columns in all insert, update, select, and row-mapper code paths.

- [ ] **Step 4: Add schema migration**

Add columns:

```sql
ALTER TABLE LP_LABEL_TEMPLATE_ELEMENT ADD visible_when_field VARCHAR(100);
ALTER TABLE LP_LABEL_TEMPLATE_ELEMENT ADD visible_when_operator VARCHAR(40);
ALTER TABLE LP_LABEL_TEMPLATE_ELEMENT ADD visible_when_value VARCHAR(255);
```

Add the same columns to the V1 schema for clean test databases.

- [ ] **Step 5: Re-run focused persistence test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=LabelPrintingPersistenceTest" test
```

Expected: PASS.

### Task 2: Evaluate Conditions During Rendering

**Files:**
- Create: `src/main/java/com/example/cx/boxlabel/rendering/ElementVisibilityEvaluator.java`
- Modify: `src/main/java/com/example/cx/boxlabel/rendering/JasperBoxLabelRenderer.java`
- Test: `src/test/java/com/example/cx/boxlabel/rendering/JasperBoxLabelRendererTest.java`

- [ ] **Step 1: Add a failing render test**

Create a template with one `STATIC_TEXT` element configured with `visibleWhenField = "brandName"`, `visibleWhenOperator = "equals"`, and `visibleWhenValue = "MISSING_BRAND"`. Render PNG and assert rendering succeeds with a non-empty file. This proves hidden elements do not break rendering.

- [ ] **Step 2: Run focused renderer test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=JasperBoxLabelRendererTest" test
```

Expected: FAIL until the renderer skips hidden elements.

- [ ] **Step 3: Implement `ElementVisibilityEvaluator`**

Support operators:

```text
empty, notEmpty, equals, notEquals, contains, startsWith, endsWith
```

Default behavior: no operator or no field means visible.

- [ ] **Step 4: Use evaluator in `JasperBoxLabelRenderer`**

Before creating a Jasper element, skip the template element if `isVisible(element, row)` returns false.

- [ ] **Step 5: Re-run focused renderer test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=JasperBoxLabelRendererTest" test
```

Expected: PASS.

### Task 3: Add Vue Template Studio Route

**Files:**
- Create: `src/main/resources/static/template-studio-vue.html`
- Modify: `src/main/java/com/example/cx/boxlabel/api/WebPageController.java`
- Test: `src/test/java/com/example/cx/boxlabel/api/TemplateStudioControllerTest.java`

- [ ] **Step 1: Add failing static route assertions**

Assert `GET /template-studio` contains:

```text
vue-template-studio
condition-builder
beginElementDrag
beginElementResize
visibleWhenOperator
createApp
```

Expected: FAIL while route still serves the legacy static page.

- [ ] **Step 2: Run focused static page test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: FAIL until the route forwards to the Vue page.

- [ ] **Step 3: Create Vue editor page**

Build a single static HTML page using Vue 3 global build. The page must:

- Load templates from `/api/label-templates`.
- Load field sources and sample values from `/api/name-conversions/{productConfigId}`.
- Edit existing `elements`.
- Drag selected elements on a mm canvas.
- Resize selected elements with a bottom-right handle.
- Bind fields for `FIELD_TEXT`, `BARCODE`, and `QRCODE`.
- Edit text, geometry, font size, bold, alignment, and condition fields.
- Save through the existing label-template element update endpoint.
- Preview through the existing template preview endpoint.

- [ ] **Step 4: Forward `/template-studio` to the Vue page**

Change `WebPageController.templateStudio()` to:

```java
return "forward:template-studio-vue.html";
```

- [ ] **Step 5: Re-run focused static page test**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=TemplateStudioControllerTest#templateStudioServesDedicatedTemplateMaintenanceWorkspace" test
```

Expected: PASS.

### Task 4: Verify Core Regression Set

**Files:**
- Test-only verification across existing backend and page tests.

- [ ] **Step 1: Run controller, persistence, and renderer tests**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn "-Dtest=BoxLabelControllerTest,TemplateStudioControllerTest,LabelPrintingPersistenceTest,JasperBoxLabelRendererTest" test
```

Expected: PASS.

- [ ] **Step 2: Run full Maven suite if the focused suite passes**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn test
```

Expected: PASS or identify unrelated pre-existing failures.

### Task 5: Local Smoke Test

**Files:**
- Runtime verification only.

- [ ] **Step 1: Start or restart local app**

Run:

```powershell
. ..\tools\use-java8-maven.ps1; mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Expected: app listens on port `8088`.

- [ ] **Step 2: Open result page**

Open:

```text
http://localhost:8088/template-studio
```

Expected: Vue editor loads with template list, canvas, element inspector, condition builder, and save/preview controls.

