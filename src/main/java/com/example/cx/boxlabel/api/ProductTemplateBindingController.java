package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.application.ProductTemplateBindingService;
import com.example.cx.boxlabel.domain.ProductTemplateBinding;
import com.example.cx.boxlabel.domain.ProductTemplateBindingRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-template-bindings")
public class ProductTemplateBindingController {

    private final ProductTemplateBindingService bindingService;

    public ProductTemplateBindingController(ProductTemplateBindingService bindingService) {
        this.bindingService = bindingService;
    }

    @GetMapping("/{productConfigId}")
    public ProductTemplateBinding get(@PathVariable String productConfigId) {
        return bindingService.getBinding(productConfigId);
    }

    @PutMapping("/{productConfigId}")
    public ProductTemplateBinding save(@PathVariable String productConfigId,
                                       @RequestBody ProductTemplateBindingRequest request) {
        return bindingService.saveBinding(productConfigId, request);
    }
}
