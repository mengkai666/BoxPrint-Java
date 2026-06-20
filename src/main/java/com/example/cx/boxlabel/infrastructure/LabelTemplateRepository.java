package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.LabelTemplateElement;

import java.util.List;

public interface LabelTemplateRepository {

    List<LabelTemplate> findAll();

    LabelTemplate findByCode(String templateCode);

    void save(LabelTemplate template);

    void replaceElements(String templateCode, List<LabelTemplateElement> elements);

    void recordImport(String importId,
                      String importSource,
                      String templateCode,
                      String labelType,
                      String legacyTemplateId,
                      String legacyTemplateName,
                      String sampleName,
                      String status,
                      String message);
}
