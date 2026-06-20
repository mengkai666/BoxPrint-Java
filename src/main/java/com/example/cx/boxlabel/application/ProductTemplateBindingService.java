package com.example.cx.boxlabel.application;

import com.example.cx.boxlabel.domain.ProductTemplateBinding;
import com.example.cx.boxlabel.domain.ProductTemplateBindingRequest;
import com.example.cx.boxlabel.infrastructure.ProductTemplateBindingRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductTemplateBindingService {

    private final BoxLabelTemplateService templateService;
    private final ProductTemplateBindingRepository bindingRepository;

    public ProductTemplateBindingService(BoxLabelTemplateService templateService,
                                         ProductTemplateBindingRepository bindingRepository) {
        this.templateService = templateService;
        this.bindingRepository = bindingRepository;
    }

    public ProductTemplateBinding getBinding(String productConfigId) {
        ProductTemplateBinding binding = bindingRepository.findByProductConfigId(productConfigId);
        if (binding == null) {
            binding = defaultBinding(productConfigId);
        }
        return copy(binding);
    }

    public synchronized ProductTemplateBinding saveBinding(String productConfigId, ProductTemplateBindingRequest request) {
        String boxTemplateCode = trimToDefault(request.getBoxTemplateCode(), "box-standard");
        String bagTemplateCode = trimToDefault(request.getBagTemplateCode(), "bag-standard");
        templateService.requireTemplate(boxTemplateCode);
        templateService.requireTemplate(bagTemplateCode);

        ProductTemplateBinding binding = new ProductTemplateBinding();
        binding.setProductConfigId(productConfigId);
        binding.setBoxTemplateCode(boxTemplateCode);
        binding.setBagTemplateCode(bagTemplateCode);
        binding.setSource("JAVA_BINDING");
        bindingRepository.save(binding);
        return copy(binding);
    }

    public String resolveTemplateCode(String productConfigId, String labelType, String explicitTemplateCode) {
        if (explicitTemplateCode != null && !explicitTemplateCode.trim().isEmpty()) {
            return explicitTemplateCode.trim();
        }
        ProductTemplateBinding binding = getBinding(productConfigId);
        String normalized = labelType == null || labelType.trim().isEmpty() ? "BOX" : labelType.trim().toUpperCase();
        if ("BAG".equals(normalized)) {
            return binding.getBagTemplateCode();
        }
        return binding.getBoxTemplateCode();
    }

    private ProductTemplateBinding defaultBinding(String productConfigId) {
        ProductTemplateBinding binding = new ProductTemplateBinding();
        binding.setProductConfigId(productConfigId);
        binding.setBoxTemplateCode("box-standard");
        binding.setBagTemplateCode("bag-standard");
        binding.setSource("DEFAULT_TEMPLATE");
        return binding;
    }

    private ProductTemplateBinding copy(ProductTemplateBinding source) {
        ProductTemplateBinding copy = new ProductTemplateBinding();
        copy.setProductConfigId(source.getProductConfigId());
        copy.setBoxTemplateCode(source.getBoxTemplateCode());
        copy.setBagTemplateCode(source.getBagTemplateCode());
        copy.setSource(source.getSource());
        return copy;
    }

    private String trimToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
