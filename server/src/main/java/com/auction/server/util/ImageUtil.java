package com.auction.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility for handling image uploads and Base64 processing. */
public class ImageUtil {
  private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);
  private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB

  /**
   * Decodes a Base64 string and saves it as a file.
   *
   * @param base64Data The Base64 encoded image data.
   * @param targetDir The directory where the image should be saved.
   * @return The filename of the saved image, or null if failed.
   */
  public static String saveBase64Image(String base64Data, String targetDir) {
    if (base64Data == null || base64Data.isEmpty()) {
      return null;
    }

    // 1. Remove prefix if present (e.g., "data:image/png;base64,")
    String pureBase64 = base64Data;
    String extension = "png"; // Default

    if (base64Data.contains(",")) {
      String prefix = base64Data.split(",")[0];
      pureBase64 = base64Data.split(",")[1];

      if (prefix.contains("image/jpeg") || prefix.contains("image/jpg")) {
        extension = "jpg";
      } else if (prefix.contains("image/gif")) {
        extension = "gif";
      }
    }

    byte[] decodedBytes;
    try {
      decodedBytes = Base64.getDecoder().decode(pureBase64);
    } catch (IllegalArgumentException e) {
      logger.error("Invalid Base64 data provided", e);
      throw new IllegalArgumentException("Invalid image data format.");
    }

    // 2. Validate size
    if (decodedBytes.length > MAX_SIZE) {
      throw new IllegalArgumentException("Image size exceeds the 2MB limit.");
    }

    // 3. Save to file
    String fileName = UUID.randomUUID().toString() + "." + extension;
    File file = new File(targetDir, fileName);

    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(decodedBytes);
      logger.info("Saved image to {}", file.getAbsolutePath());
      return fileName;
    } catch (IOException e) {
      logger.error("Failed to save image to disk", e);
      throw new IllegalStateException("Could not save image to server storage.");
    }
  }
}
