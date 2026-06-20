# Java8 Label Printing Debug Guide

This service is the pure-Java label-printing slice for the `产销协同` modernization path. It uses Spring Boot 2.7.x, Java DTOs, JasperReports, ZXing, and Barcode4J.

## 1. Start

```powershell
cd C:\Users\mengk\Desktop\code-test
. .\tools\use-java8-maven.ps1
cd .\java8-boxlabel-service
mvn clean test
mvn spring-boot:run
```

Expected:

- Tests cover API, business rules, template studio, dynamic Jasper rendering, and print jobs.
- The server starts on `http://localhost:8088/`.
- The default profile uses sample data and does not connect to a real database.

## 2. Main Debug Flow

1. Query products through `GET /api/name-conversions/products`.
2. Inspect one product through `GET /api/name-conversions/{productConfigId}`.
3. Create or import a template through `/api/label-templates` or `/api/template-imports/*`.
4. Save template elements through `PUT /api/label-templates/{code}/elements`.
5. Bind the product through `PUT /api/product-template-bindings/{productConfigId}`.
6. Render through `POST /api/box-labels/render`.
7. Print through `POST /api/box-labels/print`.
8. Read print history through `GET /api/box-labels/print-jobs`.

## 3. Useful Breakpoints

- `BoxLabelPrintService.prepareRow`: confirms template binding priority.
- `BoxLabelTemplateService.updateElements`: confirms layout version increment.
- `JasperBoxLabelRenderer.buildDesign`: confirms CONFIG_LAYOUT to JasperDesign conversion.
- `NameConversionService.detail`: confirms field source diagnostics.
- `InMemoryPrintJobRepository.recordBrowserPrint`: confirms print history capture.

## 4. QA SQL Server Profile

Copy `src/main/resources/application-local.example.properties` to `application-local.properties`, fill QA-only credentials, then run:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver,local
```

Do not commit real database credentials.
