package com.tilldock.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageService {

    private static final long MAX_BYTES = 4L * 1024L * 1024L;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 1280;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp");

    private final Path rootDir = Paths.get("uploads").toAbsolutePath().normalize();
    private final Path productsDir = rootDir.resolve("products");

    public ProductImageService() throws IOException {
        Files.createDirectories(productsDir);
    }

    public StoredImage store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Image must be 4 MB or smaller.");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String normalized = contentType.toLowerCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Only JPG, PNG, or WEBP images are allowed.");
        }

        UUID productId = UUID.randomUUID();
        String extension = extensionFor(normalized);
        String fileName = productId + extension;
        Path target = productsDir.resolve(fileName);

        BufferedImage original;
        try (InputStream in = file.getInputStream()) {
            original = ImageIO.read(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read uploaded image.");
        }
        if (original == null) {
            throw new IllegalArgumentException("Uploaded file is not a valid image.");
        }

        BufferedImage resized = resizeIfNeeded(original);
        try {
            ImageIO.write(resized, extension.equals(".png") ? "png" : "jpg", target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write image to disk.", e);
        }

        String url = "/uploads/products/" + fileName;
        return new StoredImage(url, target);
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String prefix = "/uploads/products/";
        if (!imageUrl.startsWith(prefix)) {
            return;
        }
        String fileName = imageUrl.substring(prefix.length());
        Path target = productsDir.resolve(fileName).normalize();
        if (!target.startsWith(productsDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= MAX_WIDTH && h <= MAX_HEIGHT) {
            return src;
        }
        double scale = Math.min((double) MAX_WIDTH / w, (double) MAX_HEIGHT / h);
        int targetW = Math.max(1, (int) Math.round(w * scale));
        int targetH = Math.max(1, (int) Math.round(h * scale));
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return out;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    public record StoredImage(String url, Path path) {
    }
}