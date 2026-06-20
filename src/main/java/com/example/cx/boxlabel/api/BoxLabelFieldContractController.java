package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.application.BoxLabelFieldContractService;
import com.example.cx.boxlabel.domain.BoxLabelFieldMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/box-labels")
public class BoxLabelFieldContractController {

    private final BoxLabelFieldContractService fieldContractService;

    public BoxLabelFieldContractController(BoxLabelFieldContractService fieldContractService) {
        this.fieldContractService = fieldContractService;
    }

    @GetMapping("/field-contract")
    public List<BoxLabelFieldMapping> fieldContract() {
        return fieldContractService.listMappings();
    }
}

