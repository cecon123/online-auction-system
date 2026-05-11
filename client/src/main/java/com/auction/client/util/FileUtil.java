package com.auction.client.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/** Utility for client-side file handling. */
public class FileUtil {
  private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB

  /**
   * Converts a file to a Base64 string.
   *
   * @param file The file to convert.
   * @return The Base64 encoded string with data prefix.
   * @throws IOException If file reading fails.
   */
  public static String toBase64(File file) throws IOException {
    if (file == null || !file.exists()) {
      return null;
    }

    if (file.length() > MAX_SIZE) {
      throw new IllegalArgumentException("File is too large (max 2MB)");
    }

    byte[] fileContent = Files.readAllBytes(file.toPath());
    String encoded = Base64.getEncoder().encodeToString(fileContent);

    String contentType = Files.probeContentType(file.toPath());
    if (contentType == null) {
      contentType = "image/png"; // Fallback
    }

    return "data:" + contentType + ";base64," + encoded;
  }
}
