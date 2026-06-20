package com.example.cx.boxlabel.rendering;

import com.example.cx.boxlabel.config.PrintingProperties;
import com.example.cx.boxlabel.domain.LabelOutputFormat;
import com.example.cx.boxlabel.domain.LabelTemplate;
import com.example.cx.boxlabel.domain.RenderedLabel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileSystemRenderOutputStore {

    private final PrintingProperties properties;

    public FileSystemRenderOutputStore(PrintingProperties properties) {
        this.properties = properties;
    }

    public Path resolveTemplate(LabelTemplate template) {
        return Paths.get(properties.getTemplatePath(), template.getFileName()).toAbsolutePath().normalize();
    }

    public RenderedLabel save(LabelTemplate template, LabelOutputFormat format, byte[] bytes) {
        try {
            Path outputDirectory = Paths.get(properties.getOutputPath()).toAbsolutePath().normalize();
            Files.createDirectories(outputDirectory);
            String fileId = UUID.randomUUID().toString();
            Path filePath = outputDirectory.resolve(fileId + "." + format.getExtension());
            Files.write(filePath, bytes);

            RenderedLabel renderedLabel = new RenderedLabel();
            renderedLabel.setFileId(fileId);
            renderedLabel.setFormat(format);
            renderedLabel.setPreviewUrl("/api/box-labels/files/" + fileId);
            renderedLabel.setTemplateCode(template.getCode());
            renderedLabel.setTemplateVersion(template.getVersion());
            renderedLabel.setSizeBytes(Files.size(filePath));
            renderedLabel.setFilePath(filePath);
            return renderedLabel;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store rendered label output.", e);
        }
    }

    public Path findFile(String fileId) {
        try {
            Path outputDirectory = Paths.get(properties.getOutputPath()).toAbsolutePath().normalize();
            Path pdf = outputDirectory.resolve(fileId + ".pdf");
            if (Files.exists(pdf)) {
                return pdf;
            }
            Path png = outputDirectory.resolve(fileId + ".png");
            if (Files.exists(png)) {
                return png;
            }
            throw new IllegalArgumentException("Rendered label file not found: " + fileId);
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
