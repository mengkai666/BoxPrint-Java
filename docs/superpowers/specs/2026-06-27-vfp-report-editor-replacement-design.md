# VFP Report Editor Replacement Design

## Goal

Completely replace the VFP FRX/FRT report maintenance workflow with a web-based visual template editor. The new editor must support direct drag-and-drop layout, field binding, reusable components, and conditional component visibility while keeping final print output stable and predictable.

The target is not to build a web wrapper around `MODIFY REPORT`. The target is to make FRX/FRT a legacy import/reference format and move new template maintenance into a structured JSON model controlled by the Java system.

## Current VFP Workflow

The VFP project stores printable reports as `Reports/*.FRX` and `Reports/*.FRT`. Template records live in `CD_PRINT_TEMPLATE`, where compressed FRX/FRT blobs are stored in `PT_REPORT_FRX` and `PT_REPORT_FRT`.

The VFP maintenance form `print_template` manages records, scans local `reports\*.frx`, uploads compressed report files, and opens the native VFP designer through:

```foxpro
MODIFY REPORT &frx
```

This means the current "editor" is the VFP Report Designer. It is stable for the old desktop system, but it is not a good foundation for modern web editing, rule-based visibility, browser preview, or long-term Java ownership.

## Recommended Approach

Use a Vue-based visual editor backed by a first-party Java template model.

The frontend may use hiprint for selected canvas and browser-print capabilities, but hiprint should not become the source-of-truth template format. The source of truth should be a system-owned JSON model with explicit page settings, elements, bindings, style properties, and condition rules.

This gives the best balance:

- Stability: final rendering and business rules are owned by Java instead of a browser plugin.
- Convenience: users get drag, resize, field binding, property editing, and live preview in the browser.
- Migration safety: old FRX templates can be migrated gradually without blocking current printing.
- Maintainability: template rules such as "hide when field is empty" remain explicit and testable.

## Architecture

### Frontend

Create a Vue template editor for template maintenance and printing workflows.

Main areas:

- Left panel: template list, field library, component library, reusable component presets.
- Center canvas: page surface with mm-based positioning, drag, resize, zoom, grid, alignment guides, and selection.
- Right inspector: geometry, style, field binding, barcode/QR options, image options, condition rules, and test data preview.
- Preview/print panel: render with sample data, inspect condition results, export PDF/PNG, and submit print jobs.

The editor should feel like a purpose-built print template tool rather than a large form. Most advanced fields stay collapsed until an element is selected.

### Backend

Keep Java 8 and Spring Boot as the backend foundation.

The backend owns:

- Template persistence.
- Template validation.
- Field contracts and sample values.
- Condition-rule evaluation.
- Rendering to PNG/PDF.
- Print job creation and logging.
- Legacy FRX migration metadata.

Existing APIs under `/api/label-templates`, `/api/name-conversions`, and `/api/box-labels` can be extended rather than replaced where possible.

### Rendering

Java rendering remains the authoritative final output path. Browser preview may be used for fast feedback, but saved templates must render through the backend with the same layout and condition logic used for printing.

The current Jasper renderer can continue short term. As the JSON model grows, the rendering layer should be isolated behind a `TemplateRenderEngine` boundary so the implementation can evolve without changing editor or print APIs.

## Template Model

Add a structured template document stored as JSON.

Core shape:

```json
{
  "version": 1,
  "code": "BOX_STANDARD",
  "name": "Box Standard Template",
  "labelType": "BOX",
  "page": {
    "widthMm": 100,
    "heightMm": 70,
    "dpi": 203
  },
  "elements": [
    {
      "id": "product-name",
      "type": "FIELD_TEXT",
      "fieldName": "boxLabelName",
      "leftMm": 8,
      "topMm": 8,
      "widthMm": 58,
      "heightMm": 8,
      "fontSize": 10,
      "bold": true,
      "textAlign": "left",
      "visibleWhen": {
        "mode": "all",
        "rules": [
          {
            "field": "boxLabelName",
            "operator": "notEmpty"
          }
        ]
      }
    }
  ]
}
```

Supported element types for the first replacement release:

- `STATIC_TEXT`
- `FIELD_TEXT`
- `BARCODE`
- `QRCODE`
- `IMAGE`
- `LOGO`
- `LINE`
- `RECTANGLE`

Future releases can add table, repeated band, rich text, and formula elements after the fixed-label use case is stable.

## Conditional Visibility

Conditional visibility is a first-class element property, not a frontend-only display flag.

Each element can define `visibleWhen`. If omitted, the element is visible. Conditions are evaluated by Java during preview, PDF/PNG rendering, and print submission.

Initial operators:

- `empty`
- `notEmpty`
- `equals`
- `notEquals`
- `contains`
- `startsWith`
- `endsWith`
- `in`
- `notIn`
- `greaterThan`
- `greaterThanOrEqual`
- `lessThan`
- `lessThanOrEqual`

Condition groups support:

- `all`: every rule must match.
- `any`: one rule must match.

Example use cases:

- Hide logo if the logo path or logo binary is empty.
- Show company logo only when `businessAddressName` is present.
- Show a warning text only for selected brands or label types.
- Show bag-specific content when `labelType = BAG`.
- Hide optional nutrition or storage fields when the value is empty.

The editor should include a condition builder with field picker, operator picker, value input, and a live "visible/hidden with current sample data" result.

## FRX Replacement And Migration

FRX/FRT should not be edited in the new system. They should be treated as legacy artifacts.

Migration path:

1. Inventory existing FRX templates and rank them by actual usage.
2. Pick representative box, bag, and label templates.
3. Manually recreate them in the new editor first.
4. Add optional FRX inspection tooling only if it saves real migration time.
5. Keep legacy FRX print paths available during transition.
6. Move product-template bindings from FRX names to new template codes when each template is verified.

Full automatic FRX-to-JSON conversion is out of scope for the first replacement release. FRX is a DBF-style report structure with expression semantics that are easy to misread. Manual migration with visual verification is safer for production labels.

## Data Flow

Template editing:

1. User opens the Vue template center.
2. Frontend loads templates, fields, sample products, and reusable presets.
3. User edits elements on the canvas.
4. Frontend validates obvious layout issues.
5. Backend validates schema, field names, page bounds, and condition rules.
6. Backend saves the JSON model and audit metadata.

Preview:

1. User selects sample product data.
2. Backend resolves field values.
3. Backend evaluates `visibleWhen` rules.
4. Backend renders PNG/PDF using the same engine used by printing.
5. Frontend shows the rendered result and condition diagnostics.

Print:

1. User selects product, template, printer, copies, date, shift, and operator.
2. Backend resolves the saved template and product data.
3. Backend evaluates conditions and renders final output.
4. Backend creates print job/log entries.
5. Browser print or print-service integration handles physical output.

## Validation And Error Handling

Template save validation:

- Reject unknown element types.
- Reject elements outside the page.
- Reject invalid dimensions.
- Reject unknown field bindings unless explicitly marked as custom/static.
- Reject malformed condition rules.
- Default missing `visibleWhen` to always visible.

Preview and print errors:

- Missing optional field: render empty value and include a warning.
- Missing required field: block print and show the missing field list.
- Invalid image/logo source: hide the image if configured optional; otherwise block print.
- Condition evaluation error: block save if static validation catches it; block render if a persisted rule is invalid.

The UI should show errors at the point of action and also mark the affected element in the canvas/layer list.

## Testing

Backend tests:

- Persist and read template JSON with elements and `visibleWhen`.
- Validate condition operators.
- Evaluate conditional visibility against sample print rows.
- Render templates with hidden and visible elements.
- Confirm PDF/PNG output is non-empty.
- Confirm print job flow uses the saved JSON template.

Frontend/static tests:

- Vue app route loads for template center and print workbench.
- Component library exposes text, field, barcode, QR, logo/image, line, rectangle.
- Inspector exposes field binding and condition builder.
- Canvas exposes drag, resize, zoom, grid, and layer selection markers.
- Preview displays condition diagnostics.

Migration tests:

- Representative recreated templates render close enough to approved legacy samples.
- Legacy FRX print path still works until a product binding is migrated.
- New template binding can be rolled back to the old template name during transition.

## Phased Delivery

### Phase 1: Foundation

- Introduce Vue frontend structure.
- Keep existing Java APIs working.
- Add structured template JSON persistence if current element table is not enough.
- Add backend condition-rule model and evaluator.
- Add a simple condition builder to the editor.

### Phase 2: Editor Replacement

- Build the full drag-and-drop template center.
- Add reusable component presets.
- Add live sample preview and backend render preview.
- Add validation markers and condition diagnostics.
- Support the first production box/bag/label templates.

### Phase 3: Print Workbench Integration

- Rebuild `/print-workbench` with Vue.
- Use the same field contract and template preview APIs.
- Add browser print convenience where useful.
- Keep backend rendering as final output authority.

### Phase 4: VFP Decommission Path

- Inventory high-value FRX templates.
- Recreate and approve templates in the new editor.
- Migrate product-template bindings.
- Keep rollback metadata during rollout.
- Retire VFP template upload and `MODIFY REPORT` maintenance only after active templates are covered.

## Out Of Scope For First Implementation

- Full automatic FRX-to-JSON conversion.
- Complex report bands, grouping, subreports, and pagination.
- Replacing every VFP report type outside the label/box/bag print workflow.
- Direct printer driver integration beyond the current print-job/browser-print path.
- Undo/redo collaboration, multi-user live editing, or template approval workflow.
