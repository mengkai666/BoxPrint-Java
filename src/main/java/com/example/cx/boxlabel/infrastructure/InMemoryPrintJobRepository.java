package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.BoxLabelPrintJobRequest;
import com.example.cx.boxlabel.domain.BoxLabelPrintJobResponse;
import com.example.cx.boxlabel.domain.RenderedLabel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class InMemoryPrintJobRepository implements PrintJobRepository {

    private final List<BoxLabelPrintJobResponse> jobs = new ArrayList<BoxLabelPrintJobResponse>();

    @Override
    public synchronized BoxLabelPrintJobResponse recordBrowserPrint(BoxLabelPrintJobRequest request, RenderedLabel renderedLabel) {
        BoxLabelPrintJobResponse response = new BoxLabelPrintJobResponse();
        response.setSuccess(true);
        response.setJobId(UUID.randomUUID().toString());
        response.setStatus("BROWSER_PRINT_READY");
        response.setProductConfigId(request.getProductConfigId());
        response.setLabelType(request.getLabelType() == null || request.getLabelType().trim().isEmpty()
                ? "BOX"
                : request.getLabelType().trim().toUpperCase());
        response.setPrinterName(request.getPrinterName());
        response.setCopies(request.getCopies());
        response.setOperator(request.getOperator());
        response.setOutput(renderedLabel);
        jobs.add(0, copy(response));
        return response;
    }

    @Override
    public synchronized List<BoxLabelPrintJobResponse> listRecent() {
        List<BoxLabelPrintJobResponse> copy = new ArrayList<BoxLabelPrintJobResponse>();
        for (BoxLabelPrintJobResponse job : jobs) {
            copy.add(copy(job));
        }
        return Collections.unmodifiableList(copy);
    }

    private BoxLabelPrintJobResponse copy(BoxLabelPrintJobResponse source) {
        BoxLabelPrintJobResponse copy = new BoxLabelPrintJobResponse();
        copy.setSuccess(source.isSuccess());
        copy.setJobId(source.getJobId());
        copy.setStatus(source.getStatus());
        copy.setProductConfigId(source.getProductConfigId());
        copy.setLabelType(source.getLabelType());
        copy.setPrinterName(source.getPrinterName());
        copy.setCopies(source.getCopies());
        copy.setOperator(source.getOperator());
        copy.setOutput(source.getOutput());
        return copy;
    }
}
