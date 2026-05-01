package com.ihg.b2b;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {

    /** URL that will be encoded into the QR code. Change this as needed. */
    private static final String URL = "https://www.behance.net/ghlontinutsa";

    /** Size of each QR module (pixel) in the SVG. */
    private static final int MODULE_SIZE = 10;

    /** Quiet zone (modules) around the QR code. */
    private static final int QUIET_ZONE = 4;

    public static void main(String[] args) throws WriterException, IOException {

        // --- 1. Generate QR bit-matrix via ZXing ---
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, QUIET_ZONE);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(URL, BarcodeFormat.QR_CODE, 0, 0, hints);

        int matrixWidth  = matrix.getWidth();
        int matrixHeight = matrix.getHeight();

        int svgWidth  = matrixWidth  * MODULE_SIZE;
        int svgHeight = matrixHeight * MODULE_SIZE;

        // --- 2. Build SVG content ---
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" " +
                "width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                svgWidth, svgHeight, svgWidth, svgHeight));
        svg.append("  <!-- QR code for: ").append(URL).append(" -->\n");

        // White background
        svg.append(String.format(
                "  <rect width=\"%d\" height=\"%d\" fill=\"white\"/>\n",
                svgWidth, svgHeight));

        // Dark modules
        svg.append("  <g fill=\"black\">\n");
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                if (matrix.get(x, y)) {
                    svg.append(String.format(
                            "    <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>\n",
                            x * MODULE_SIZE, y * MODULE_SIZE, MODULE_SIZE, MODULE_SIZE));
                }
            }
        }
        svg.append("  </g>\n");
        svg.append("</svg>\n");

        // --- 3. Write SVG to src/main/resources ---
        Path resourcesDir = Paths.get("src", "main", "resources");
        Files.createDirectories(resourcesDir);

        Path outputFile = resourcesDir.resolve("qrcode.svg");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            pw.print(svg);
        }

        System.out.println("QR code SVG saved to: " + outputFile.toAbsolutePath());
    }
}
