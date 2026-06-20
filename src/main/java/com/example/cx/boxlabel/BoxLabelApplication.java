package com.example.cx.boxlabel;

import com.example.cx.boxlabel.config.PrintingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PrintingProperties.class)
public class BoxLabelApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoxLabelApplication.class, args);
    }
}
