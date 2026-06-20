package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class BoxLabelBusinessRules {

    private static final DateTimeFormatter CHINESE_DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    public BoxLabelPrintRow prepare(BoxLabelPrintRow row, LocalDate productionDate, String shift) {
        LocalDate safeProductionDate = productionDate == null ? LocalDate.now() : productionDate;
        row.setProductionDate(CHINESE_DATE.format(safeProductionDate));
        row.setShift(emptyToDefault(shift, emptyToDefault(row.getShift(), "A")));
        row.setBoxLabelName(formatTitle(emptyToDefault(row.getBoxLabelName(), row.getInventoryName())));
        applyMemoRules(row);
        applyShelfLife(row, safeProductionDate);
        applyBarcodeFallback(row);
        return row;
    }

    String formatTitle(String title) {
        String value = emptyToDefault(title, "");
        int chineseOrFullWidth = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 127) {
                chineseOrFullWidth++;
            }
        }
        if (chineseOrFullWidth > 6 || chineseOrFullWidth == 0) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(value.charAt(i));
        }
        return builder.toString();
    }

    private void applyMemoRules(BoxLabelPrintRow row) {
        String memo = emptyToDefault(row.getAddMemo(), "");
        if (memo.startsWith("水印")) {
            row.setWatermark(memo.substring("水印".length()));
            row.setAddMemo("");
            return;
        }
        if (memo.startsWith("专供") && memo.length() > "专供".length()) {
            row.setBrandName(memo.substring("专供".length()));
            row.setAddMemo("专供");
        }
    }

    private void applyShelfLife(BoxLabelPrintRow row, LocalDate productionDate) {
        if (!Boolean.TRUE.equals(row.getShelfLifeConvert())) {
            return;
        }
        String shelfLife = emptyToDefault(row.getShelfLife(), "");
        LocalDate expiryDate;
        if (shelfLife.contains("月")) {
            expiryDate = productionDate.plusMonths(numberPrefix(shelfLife)).minusDays(1);
        } else if (shelfLife.contains("年")) {
            expiryDate = productionDate.plusYears(numberPrefix(shelfLife)).minusDays(1);
        } else {
            expiryDate = productionDate.plusDays(numberPrefix(shelfLife)).minusDays(1);
        }
        row.setShelfLife("至" + CHINESE_DATE.format(expiryDate));
    }

    private int numberPrefix(String value) {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isDigit(current)) {
                digits.append(current);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() == 0) {
            return 0;
        }
        return Integer.parseInt(digits.toString());
    }

    private void applyBarcodeFallback(BoxLabelPrintRow row) {
        if (isBlank(row.getProductBarcode())) {
            if (!isBlank(row.getOriginalCustomerBarcode())) {
                row.setProductBarcode(row.getOriginalCustomerBarcode().trim());
            } else if (!isBlank(row.getOriginalProductBarcode())) {
                row.setProductBarcode(row.getOriginalProductBarcode().trim());
            }
        }
        if (row.getBarcodeType() == null) {
            row.setBarcodeType(151);
        }
    }

    private String emptyToDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
