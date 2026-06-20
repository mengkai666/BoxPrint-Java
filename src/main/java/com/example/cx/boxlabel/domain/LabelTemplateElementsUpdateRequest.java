package com.example.cx.boxlabel.domain;

import java.util.ArrayList;
import java.util.List;

public class LabelTemplateElementsUpdateRequest {

    private List<LabelTemplateElement> elements = new ArrayList<LabelTemplateElement>();

    public List<LabelTemplateElement> getElements() {
        return elements;
    }

    public void setElements(List<LabelTemplateElement> elements) {
        this.elements = elements == null ? new ArrayList<LabelTemplateElement>() : elements;
    }
}
