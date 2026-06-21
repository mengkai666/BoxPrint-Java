# Template Studio Slimming Design

## Goal

Make the template maintenance center feel lighter and more edit-focused without changing APIs, schemas, rendering behavior, or the print workbench.

## Current Problem

The template studio shows every field group expanded by default, places field and layer lists above the canvas, and gives the same visual weight to auxiliary lists as to the actual editing surface. This makes the maintenance center feel bulky even though the core editing loop now works.

## Recommended Approach

Use a focused, low-risk UI tightening:

- Keep the three-column page and existing static HTML implementation.
- Make the middle editor prioritize the canvas, with a compact tools band above it.
- Move field palette and layer list into a compact support grid below the canvas.
- Add field search so users can quickly find a field by display name, Java field name, or category.
- Add a small pinned "常用字段" group for the most common label fields.
- Collapse regular field groups by default, with only the pinned group open.
- Keep all edit, save, copy, preview, and import actions wired to existing APIs.

## Behavior

When `/template-studio` loads, a CONFIG_LAYOUT template is still selected first. The field panel shows a search input, a pinned common-field group, and collapsed grouped fields. Typing in field search filters visible fields and opens groups that contain matches. Clearing the search restores the compact default. Clicking any field still adds a `FIELD_TEXT` element.

The layer list remains available but no longer competes with the canvas as the first large visible block. Existing selection, duplicate, align, layer move, save elements, copy template, and sample preview behavior stays unchanged.

## Tests

Extend the existing static page test to assert the maintenance center exposes:

- `fieldSearch`
- `commonFieldNames`
- `renderFieldSearch`
- `fieldPaletteSupport`
- `support-grid`
- `shouldOpenFieldGroup`
- `常用字段`

Run the template studio page test, then the focused controller tests and full Maven suite after implementation.

## Out Of Scope

- No backend changes.
- No new framework or build tooling.
- No template rendering changes.
- No print workbench redesign in this iteration.
