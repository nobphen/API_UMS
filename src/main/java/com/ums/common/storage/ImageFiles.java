package com.ums.common.storage;

import com.ums.common.exception.BusinessException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image uploads in 2 steps:
 * <pre>
 * 1. require(file, maxMb)            → is it really a JPG or PNG? not too big?   (the checks, 400 when wrong)
 * 2. clean(file, extension, maxSide) → the bytes to save: upright, smaller, WITHOUT the hidden data
 * </pre>
 * The type is read from the first bytes of the file (its "magic number"), never from the name or the
 * Content-Type sent by the client: both can lie (virus.exe renamed to photo.jpg).
 * Cleaning = draw the picture again into a new file: the EXIF data of the phone (GPS position of the
 * home, phone model, date) is not copied, so nobody can read it from the photo.
 */
public final class ImageFiles {

    /** How each image type starts. */
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    /** Biggest picture we agree to open (width × height): a small file can hide a huge picture that fills the memory. */
    private static final int MAX_MEGAPIXELS = 40;

    /** JPG quality when saving again: 0.9 = looks the same, much smaller than the phone's file. */
    private static final float JPG_QUALITY = 0.9f;

    /** Only static methods. */
    private ImageFiles() {
    }

    /**
     * Checks the file and gives its real extension.
     *
     * @param file  the upload
     * @param maxMb the biggest size allowed, in MB
     * @return "jpg" or "png"
     * @throws BusinessException error.file.empty (no file or 0 bytes); error.file.too_large (bigger than maxMb);
     *                           error.file.not_image (not a JPG or PNG)
     */
    public static String require(MultipartFile file, int maxMb) {

        // 1. A file was sent
        if (file == null || file.isEmpty()) {
            throw new BusinessException("error.file.empty");
        }

        // 2. Not too big
        if (file.getSize() > maxMb * 1024L * 1024L) {
            throw new BusinessException("error.file.too_large", maxMb);
        }

        // 3. The first bytes say the real type
        byte[] head = firstBytes(file, 8);
        if (startsWith(head, JPG)) {
            return "jpg";
        }
        if (startsWith(head, PNG)) {
            return "png";
        }
        throw new BusinessException("error.file.not_image");
    }

    /**
     * Draws the picture again: turned upright (phones save some photos sideways + a "rotate me" note in EXIF),
     * made smaller when bigger than maxSide, and saved without any hidden data (no GPS).
     *
     * @param file      the upload, already checked by {@link #require}
     * @param extension "jpg" or "png" from {@link #require}
     * @param maxSide   the longest side after cleaning, in pixels (e.g. 1024 for a profile photo)
     * @return the bytes to store
     * @throws BusinessException error.file.not_image (the file is broken or can't be opened);
     *                           error.file.too_many_pixels (more than 40 megapixels)
     */
    public static byte[] clean(MultipartFile file, String extension, int maxSide) {
        byte[] original = bytes(file);

        // 1. Check the size of the picture BEFORE opening it (reads only the header)
        requireSmallEnough(original);

        // 2. Open the picture (a broken or strange file → not an image)
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(original));
        } catch (IOException | RuntimeException e) {
            throw new BusinessException("error.file.not_image");
        }
        if (image == null) {
            throw new BusinessException("error.file.not_image");
        }

        // 3. Upright: the EXIF "orientation" of the phone (JPG only)
        if ("jpg".equals(extension)) {
            image = rotate(image, orientation(original));
        }

        // 4. Smaller when needed (same shape)
        image = shrink(image, maxSide, "png".equals(extension));

        // 5. Save again: only the pixels are written, the EXIF data is gone
        return "jpg".equals(extension) ? writeJpg(image) : writePng(image);
    }

    /// HELPERS ------------------------------------------------------

    /** Reads width and height from the header only, and refuses a picture with more than MAX_MEGAPIXELS. */
    private static void requireSmallEnough(byte[] data) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new BusinessException("error.file.not_image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > MAX_MEGAPIXELS * 1_000_000L) {
                    throw new BusinessException("error.file.too_many_pixels", MAX_MEGAPIXELS);
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new BusinessException("error.file.not_image");
        }
    }

    /**
     * The EXIF orientation of a JPG: 1 = upright, 3 = upside down, 6 = turn right, 8 = turn left (1 when unknown).
     * <pre>
     * JPG = FF D8 | segment | segment | ... | FF DA (picture data)
     * segment FF E1 "Exif\0\0" → TIFF: byte order (II / MM), first list of tags, tag 0x0112 = orientation
     * </pre>
     */
    private static int orientation(byte[] jpg) {
        int pos = 2;   // after FF D8
        while (pos + 4 <= jpg.length && (jpg[pos] & 0xFF) == 0xFF) {
            int marker = jpg[pos + 1] & 0xFF;
            int length = ((jpg[pos + 2] & 0xFF) << 8) | (jpg[pos + 3] & 0xFF);
            if (marker == 0xDA) {
                return 1;   // picture data starts: no EXIF before it
            }
            int start = pos + 4;   // the segment's content
            if (marker == 0xE1 && start + 14 <= jpg.length
                    && jpg[start] == 'E' && jpg[start + 1] == 'x' && jpg[start + 2] == 'i' && jpg[start + 3] == 'f') {
                int tiff = start + 6;
                boolean little = jpg[tiff] == 'I';   // "II" = little endian, "MM" = big endian
                int ifd = tiff + read(jpg, tiff + 4, 4, little);
                if (ifd + 2 > jpg.length) {
                    return 1;
                }
                int count = read(jpg, ifd, 2, little);
                for (int i = 0; i < count; i++) {
                    int entry = ifd + 2 + i * 12;
                    if (entry + 12 > jpg.length) {
                        return 1;
                    }
                    if (read(jpg, entry, 2, little) == 0x0112) {
                        return read(jpg, entry + 8, 2, little);
                    }
                }
                return 1;
            }
            pos += 2 + length;
        }
        return 1;
    }

    /** A number of 2 or 4 bytes, in the byte order of the EXIF data. */
    private static int read(byte[] data, int at, int size, boolean little) {
        if (at < 0 || at + size > data.length) {
            return 0;
        }
        int value = 0;
        for (int i = 0; i < size; i++) {
            int b = data[at + (little ? size - 1 - i : i)] & 0xFF;
            value = (value << 8) | b;
        }
        return value;
    }

    /** Turns the picture as the orientation says (6 = 90° right, 3 = 180°, 8 = 90° left); others unchanged. */
    private static BufferedImage rotate(BufferedImage image, int orientation) {
        int degrees = switch (orientation) {
            case 3 -> 180;
            case 6 -> 90;
            case 8 -> 270;
            default -> 0;
        };
        if (degrees == 0) {
            return image;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        boolean sideways = degrees != 180;
        BufferedImage turned = new BufferedImage(sideways ? h : w, sideways ? w : h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = turned.createGraphics();
        g.translate(turned.getWidth() / 2.0, turned.getHeight() / 2.0);   // turn around the middle
        g.rotate(Math.toRadians(degrees));
        g.drawImage(image, -w / 2, -h / 2, null);
        g.dispose();
        return turned;
    }

    /** Makes the longest side maxSide (keeps the shape); a small picture stays as it is. */
    private static BufferedImage shrink(BufferedImage image, int maxSide, boolean keepTransparency) {
        int w = image.getWidth();
        int h = image.getHeight();
        double scale = Math.min(1.0, (double) maxSide / Math.max(w, h));
        int newW = Math.max(1, (int) Math.round(w * scale));
        int newH = Math.max(1, (int) Math.round(h * scale));
        BufferedImage result = new BufferedImage(newW, newH,
                keepTransparency ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, newW, newH, null);
        g.dispose();
        return result;
    }

    /** Saves as JPG with JPG_QUALITY. */
    private static byte[] writeJpg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(stream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPG_QUALITY);
            writer.write(null, new IIOImage(image, null, null), param);   // null metadata = no EXIF
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the JPG", e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    /** Saves as PNG. */
    private static byte[] writePng(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the PNG", e);
        }
        return out.toByteArray();
    }

    /** All bytes of the upload. */
    private static byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }
    }

    /** The first n bytes of the file (fewer when the file is shorter). */
    private static byte[] firstBytes(MultipartFile file, int n) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(n);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }
    }

    /** Whether data starts with the bytes of prefix. */
    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
