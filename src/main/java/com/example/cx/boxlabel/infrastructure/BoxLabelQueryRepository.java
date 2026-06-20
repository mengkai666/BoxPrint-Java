package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.BoxLabelPrintRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;

import java.util.List;

public interface BoxLabelQueryRepository {

    List<BoxLabelProductSummary> searchProducts(ProductSearchCriteria criteria);

    BoxLabelPrintRow findBoxLabelRow(BoxLabelPrintRequest request);
}
