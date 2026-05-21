package com.auction.client.util;

/** Utility to generate URLs for assets hosted on the server. */
public class ImageUrlUtil {
  private static final String SERVER_HOST = "localhost"; // Ideally from a config file
  private static final int ASSET_PORT = 8081;

  /**
   * Gets the full URL for an image.
   *
   * @param imagePath The relative path or filename stored in DB.
   * @return The absolute HTTP URL.
   */
  public static String getImageUrl(String imagePath) {
    if (imagePath == null || imagePath.isBlank()) {
      return null;
    }

    // If it's already a full URL or absolute path (fallback for old mock data)
    if (imagePath.startsWith("http")
        || imagePath.startsWith("file:")
        || imagePath.contains(":\\")) {
      return imagePath;
    }

    // Otherwise, construct URL to asset server
    return "http://" + SERVER_HOST + ":" + ASSET_PORT + "/" + imagePath;
  }
}
