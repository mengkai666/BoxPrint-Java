package com.example.cx.boxlabel.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String root() {
        return "forward:print-workbench.html";
    }

    @GetMapping("/print-workbench")
    public String printWorkbench() {
        return "forward:print-workbench.html";
    }

    @GetMapping("/template-studio")
    public String templateStudio() {
        return "forward:template-studio-vue.html";
    }
}
