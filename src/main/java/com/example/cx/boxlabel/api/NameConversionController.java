package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.application.NameConversionService;
import com.example.cx.boxlabel.domain.BoxLabelProductSummary;
import com.example.cx.boxlabel.domain.NameConversionDetail;
import com.example.cx.boxlabel.domain.ProductSearchCriteria;
import com.example.cx.boxlabel.domain.SearchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/name-conversions")
public class NameConversionController {

    private final NameConversionService nameConversionService;

    public NameConversionController(NameConversionService nameConversionService) {
        this.nameConversionService = nameConversionService;
    }

    @GetMapping("/products")
    public SearchResult<BoxLabelProductSummary> products(ProductSearchCriteria criteria) {
        return nameConversionService.searchProducts(criteria);
    }

    @GetMapping("/{productConfigId}")
    public NameConversionDetail detail(@PathVariable String productConfigId) {
        return nameConversionService.detail(productConfigId);
    }
}
