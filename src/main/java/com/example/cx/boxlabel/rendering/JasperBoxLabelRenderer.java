package com.example.cx.boxlabel.rendering;

import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.LabelTemplateElement;
import com.example.cx.boxlabel.domain.RenderedLabel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignRectangle;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JasperBoxLabelRenderer implements BoxLabelRenderer {

    private final BarcodeImageService barcodeImageService;
    private final FileSystemRenderOutputStore outputStore;
    private final ElementVisibilityEvaluator visibilityEvaluator = new ElementVisibilityEvaluator();

    public JasperBoxLabelRenderer(BarcodeImageService barcodeImageService,
                                  FileSystemRenderOutputStore outputStore) {
        this.barcodeImageService = barcodeImageService;
        this.outputStore = outputStore;
    }

    @Override
    public RenderedLabel render(BoxLabelPrintRow row, LabelTemplate template, LabelOutputFormat format) {
        try {
            JasperReport report = compile(row, template);
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("QR_IMAGE", barcodeImageService.qrCode(row.getQrCode(), 180));
            parameters.put("BARCODE_IMAGE", barcodeImageService.code128(row.getProductBarcode()));
            parameters.put("LOGO_IMAGE", loadOptionalLogo(row.getLogoPath()));
            JasperPrint print = JasperFillManager.fillReport(
                    report,
                    parameters,
                    new JRBeanCollectionDataSource(Collections.singletonList(row))
            );
            byte[] bytes = export(print, format);
            return outputStore.save(template, format, bytes);
        } catch (JRException e) {
            throw new IllegalStateException("Unable to render Jasper box label.", e);
        }
    }

    private JasperReport compile(BoxLabelPrintRow row, LabelTemplate template) throws JRException {
        if ("CONFIG_LAYOUT".equals(template.getEngine())) {
            return JasperCompileManager.compileReport(buildDesign(row, template));
        }
        Path templatePath = outputStore.resolveTemplate(template);
        return JasperCompileManager.compileReport(templatePath.toString());
    }

    private JasperDesign buildDesign(BoxLabelPrintRow row, LabelTemplate template) throws JRException {
        JasperDesign design = new JasperDesign();
        design.setName(("label_" + template.getCode()).replaceAll("[^A-Za-z0-9_]", "_"));
        int pageWidth = mmToPoint(template.getPageWidthMm() <= 0 ? 120 : template.getPageWidthMm());
        int pageHeight = mmToPoint(template.getPageHeightMm() <= 0 ? 80 : template.getPageHeightMm());
        design.setPageWidth(pageWidth);
        design.setPageHeight(pageHeight);
        design.setLeftMargin(0);
        design.setRightMargin(0);
        design.setTopMargin(0);
        design.setBottomMargin(0);
        design.setColumnWidth(pageWidth);

        addImageParameter(design, "QR_IMAGE");
        addImageParameter(design, "BARCODE_IMAGE");
        addImageParameter(design, "LOGO_IMAGE");
        addFields(design, template.getElements(), row);

        JRDesignBand detail = new JRDesignBand();
        detail.setHeight(pageHeight);
        for (LabelTemplateElement element : sortedElements(template.getElements())) {
            if (!visibilityEvaluator.isVisible(element, row)) {
                continue;
            }
            addElement(detail, element);
        }
        ((JRDesignSection) design.getDetailSection()).addBand(detail);
        return design;
    }

    private void addFields(JasperDesign design, List<LabelTemplateElement> elements, BoxLabelPrintRow row) throws JRException {
        Set<String> names = new HashSet<String>();
        for (LabelTemplateElement element : elements) {
            if (!visibilityEvaluator.isVisible(element, row)) {
                continue;
            }
            if ("FIELD_TEXT".equals(element.getType()) && element.getFieldName() != null && !element.getFieldName().trim().isEmpty()) {
                names.add(element.getFieldName().trim());
            }
        }
        for (String name : names) {
            JRDesignField field = new JRDesignField();
            field.setName(name);
            field.setValueClass(String.class);
            design.addField(field);
        }
    }

    private void addImageParameter(JasperDesign design, String name) throws JRException {
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(name);
        parameter.setValueClass(Image.class);
        design.addParameter(parameter);
    }

    private List<LabelTemplateElement> sortedElements(List<LabelTemplateElement> elements) {
        List<LabelTemplateElement> copy = new ArrayList<LabelTemplateElement>(elements == null ? Collections.<LabelTemplateElement>emptyList() : elements);
        Collections.sort(copy, new java.util.Comparator<LabelTemplateElement>() {
            @Override
            public int compare(LabelTemplateElement left, LabelTemplateElement right) {
                return Integer.valueOf(left.getSortOrder()).compareTo(right.getSortOrder());
            }
        });
        return copy;
    }

    private void addElement(JRDesignBand detail, LabelTemplateElement element) {
        String type = element.getType() == null ? "STATIC_TEXT" : element.getType();
        if ("FIELD_TEXT".equals(type)) {
            JRDesignTextField textField = new JRDesignTextField();
            place(textField, element);
            applyTextStyle(textField, element);
            JRDesignExpression expression = new JRDesignExpression();
            expression.setText("$F{" + element.getFieldName() + "}");
            textField.setExpression(expression);
            detail.addElement(textField);
        } else if ("BARCODE".equals(type)) {
            detail.addElement(imageElement(element, "BARCODE_IMAGE"));
        } else if ("QRCODE".equals(type)) {
            detail.addElement(imageElement(element, "QR_IMAGE"));
        } else if ("LOGO".equals(type)) {
            detail.addElement(imageElement(element, "LOGO_IMAGE"));
        } else if ("LINE".equals(type)) {
            JRDesignLine line = new JRDesignLine();
            place(line, element);
            detail.addElement(line);
        } else if ("RECTANGLE".equals(type)) {
            JRDesignRectangle rectangle = new JRDesignRectangle();
            place(rectangle, element);
            detail.addElement(rectangle);
        } else {
            JRDesignStaticText text = new JRDesignStaticText();
            place(text, element);
            text.setText(element.getText() == null ? "" : element.getText());
            applyTextStyle(text, element);
            detail.addElement(text);
        }
    }

    private JRDesignImage imageElement(LabelTemplateElement element, String parameterName) {
        JRDesignImage image = new JRDesignImage(null);
        place(image, element);
        JRDesignExpression expression = new JRDesignExpression();
        expression.setText("$P{" + parameterName + "}");
        image.setExpression(expression);
        return image;
    }

    private void place(net.sf.jasperreports.engine.design.JRDesignElement designElement, LabelTemplateElement element) {
        designElement.setX(mmToPoint(element.getLeftMm()));
        designElement.setY(mmToPoint(element.getTopMm()));
        designElement.setWidth(mmToPoint(element.getWidthMm() <= 0 ? 20 : element.getWidthMm()));
        designElement.setHeight(mmToPoint(element.getHeightMm() <= 0 ? 6 : element.getHeightMm()));
    }

    private int mmToPoint(double mm) {
        return Math.max(1, (int) Math.round(mm / 25.4d * 72d));
    }

    private float fontSize(LabelTemplateElement element) {
        return element.getFontSize() <= 0 ? 10f : (float) element.getFontSize();
    }

    private void applyTextStyle(net.sf.jasperreports.engine.design.JRDesignTextElement textElement,
                                LabelTemplateElement element) {
        textElement.setFontSize(Float.valueOf(fontSize(element)));
        textElement.setBold(element.isBold());
        textElement.setHorizontalTextAlign(horizontalTextAlign(element));
    }

    private HorizontalTextAlignEnum horizontalTextAlign(LabelTemplateElement element) {
        String value = element.getTextAlign() == null ? "" : element.getTextAlign().trim().toLowerCase();
        if ("center".equals(value)) {
            return HorizontalTextAlignEnum.CENTER;
        }
        if ("right".equals(value)) {
            return HorizontalTextAlignEnum.RIGHT;
        }
        return HorizontalTextAlignEnum.LEFT;
    }

    private byte[] export(JasperPrint print, LabelOutputFormat format) throws JRException {
        if (format == LabelOutputFormat.PDF) {
            return JasperExportManager.exportReportToPdf(print);
        }
        try {
            Image image = JasperPrintManager.printPageToImage(print, 0, 2.0f);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write((BufferedImage) image, "png", output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to export Jasper label to PNG.", e);
        }
    }

    private Image loadOptionalLogo(String logoPath) {
        if (logoPath == null || logoPath.trim().isEmpty()) {
            return null;
        }
        try {
            return ImageIO.read(new java.io.File(logoPath));
        } catch (IOException e) {
            return null;
        }
    }
}
