package com.example.cx.boxlabel.rendering;

import com.example.cx.boxlabel.domain.BoxLabelPrintRow;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.RenderedLabel;

public interface BoxLabelRenderer {

    RenderedLabel render(BoxLabelPrintRow row, LabelTemplate template, LabelOutputFormat format);
}
