package com.example.cx.boxlabel.rendering;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Component
public class BarcodeImageService {

    public BufferedImage qrCode(String payload, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new MultiFormatWriter().encode(safePayload(payload), BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            return image;
        } catch (WriterException e) {
            throw new IllegalStateException("Unable to generate QR code image.", e);
        }
    }

    public BufferedImage code128(String payload) {
        try {
            Code128Bean bean = new Code128Bean();
            bean.setModuleWidth(UnitConv.in2mm(1.0f / 203));
            bean.setHeight(12);
            bean.setQuietZone(2);
            bean.doQuietZone(true);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    output,
                    "image/png",
                    203,
                    BufferedImage.TYPE_BYTE_BINARY,
                    false,
                    0
            );
            bean.generateBarcode(canvas, safePayload(payload));
            canvas.finish();
            return ImageIO.read(new ByteArrayInputStream(output.toByteArray()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to generate Code128 image.", e);
        }
    }

    private String safePayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return "EMPTY";
        }
        return payload.trim();
    }
}
