package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.BoxLabelPrintJobRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobResponse;
import com.example.cx.boxlabel.domain.RenderedLabel;

import java.util.List;

public interface PrintJobRepository {

    BoxLabelPrintJobResponse recordBrowserPrint(BoxLabelPrintJobRequest request, RenderedLabel renderedLabel);

    List<BoxLabelPrintJobResponse> listRecent();
}
