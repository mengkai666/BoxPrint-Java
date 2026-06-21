# Label Printing Database Plan

This module is designed so today's fake local database can be replaced by a real SQL Server database later without changing the API flow.

## Runtime Modes

- Default mode uses H2 in memory. Flyway creates `LP_*` tables and seeds fake data.
- `sqlserver,local` mode is reserved for QA SQL Server. The same `LP_*` tables can be created by Flyway in SQL Server.
- Real connection strings must stay in `application-local.properties` and must not be committed.

## Java-Owned Tables

These tables are owned by the Java label-printing module:

- `LP_LABEL_TEMPLATE`: template header, label type, engine, version, status.
- `LP_LABEL_TEMPLATE_ELEMENT`: configurable layout elements for `CONFIG_LAYOUT` templates.
- `LP_PRODUCT_TEMPLATE_BINDING`: product config to box/bag template binding.
- `LP_PRINT_JOB`: browser-print job log and rendered file metadata.
- `LP_TEMPLATE_IMPORT_LOG`: legacy/image import audit log.

The current seed data includes two box-label templates:

- `box-standard`: a fixed JasperReports template for the first stable sample.
- `box-config-standard`: an editable `CONFIG_LAYOUT` template with field, logo, barcode, QR code, line, and border elements. This is the first Java-owned configurable layout and should be used to validate online template editing before adding more variants.

## Legacy Tables Are Preserved

Existing production-sales and U8 tables remain source systems. Java must not rename, delete, or replace them during this phase.

Key legacy read/import sources:

- `UFDATA_001_2018.dbo.hsh_Name_Conversion`
- `UFDATA_001_2018.dbo.Inventory`
- `CD_PRINT_TEMPLATE`
- `CD_BRAND_LOGO`
- `CD_BUSINESS_ADDRESS`
- `CD_BUSINESS_PRODUCT_STANDARD`
- `CD_STORE_MODE`
- `hsh_Name_Conversion_Product_Standard`

The Java system reads these tables for product label data, field-source diagnostics, and legacy template import metadata. New template edits and bindings are written to `LP_*` tables.

## Fake Data Contract

Fake H2 data is intentionally shaped like the future SQL Server access path:

- Java-owned `LP_*` tables are created by Flyway and should be portable to SQL Server.
- Fake legacy tables are only development stand-ins for the old read-only sources.
- API requests should not depend on H2-only behavior.
- Product-template binding must be the source of truth when preview/render/print requests omit an explicit template code.
- Print jobs must keep the product config id, label type, operator, printer, copies, template code, template version, output file id, and preview URL so historical records remain readable after template changes.

## Phase 2.1 Fake Data

The local H2 legacy stand-ins include richer product scenarios so development can continue before QA SQL Server access is available:

- `DEMO-BOX-NORMAL`: complete normal box-label product.
- `DEMO-BOX-CONSIGNOR`: product with consignor name, phone, address, and type from `CD_BRAND_LOGO`.
- `DEMO-BOX-SUPERMARKET`: supermarket/customer-specific sample with customer barcode fallback behavior.
- `DEMO-BOX-125`: package size includes `125` for variant template acceptance.
- `DEMO-BOX-LONGTEXT`: long ingredients, nutrition facts, instructions, allergen, and reminder text for wrapping/rendering tests.
- `DEMO-BOX-MISSING`: intentionally missing product-standard relationship so diagnostics report missing required fields.

These rows remain fake legacy data. They do not change the ownership rule: template definitions, template elements, product-template bindings, print jobs, and import logs are still Java-owned `LP_*` data.

## Migration Boundary

The first production conversion should follow this rule:

1. Keep legacy tables as read-only references for label data and import.
2. Create `LP_*` tables in the chosen Java business database.
3. Import or bind products into `LP_PRODUCT_TEMPLATE_BINDING`.
4. Render and print from Java templates.
5. Keep old `CD_PRINT_TEMPLATE` records available for comparison until the migrated templates pass sample acceptance.

## Phase 1 Acceptance

Phase 1 is accepted when the following path works against fake data and can be repeated against QA SQL Server without API changes:

1. Query a product from Name Conversion data.
2. Inspect field-source diagnostics.
3. Bind the product to `box-config-standard`.
4. Preview the box label without passing an explicit template code.
5. Render PDF and PNG.
6. Submit a browser print job.
7. Query `LP_PRINT_JOB` through `/api/box-labels/print-jobs` and see the product, label type, operator, template, version, and output file metadata.
