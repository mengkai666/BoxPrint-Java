package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.ProductTemplateBinding;

public interface ProductTemplateBindingRepository {

    ProductTemplateBinding findByProductConfigId(String productConfigId);

    void save(ProductTemplateBinding binding);
}
