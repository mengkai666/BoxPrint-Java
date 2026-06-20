package com.example.cx.boxlabel.api;

import com.example.cx.boxlabel.application.BoxLabelFieldContractService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoxLabelFieldContractController.class)
@Import(BoxLabelFieldContractService.class)
class BoxLabelFieldContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsFieldContract() throws Exception {
        mockMvc.perform(get("/api/box-labels/field-contract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vfpFieldName").value("箱贴模版"))
                .andExpect(jsonPath("$[0].javaFieldName").value("boxTemplateName"));
    }
}

