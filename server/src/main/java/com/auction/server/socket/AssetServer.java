package com.auction.server.socket;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A simple embedded HTTP server to serve static auction images. */
public class AssetServer {
  private static final Logger logger = LoggerFactory.getLogger(AssetServer.class);
  private final int port;
  private final String assetDir;
  private HttpServer server;

  public AssetServer(int port, String assetDir) {
    this.port = port;
    this.assetDir = assetDir;
  }

  public void start() {
    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", new StaticFileHandler(assetDir));
      server.setExecutor(null); // Default executor
      server.start();
      logger.info("Asset server started on port {} serving from {}", port, assetDir);
    } catch (IOException e) {
      logger.error("Failed to start Asset server", e);
    }
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
      logger.info("Asset server stopped.");
    }
  }

  static class StaticFileHandler implements HttpHandler {
    private final String baseDir;

    public StaticFileHandler(String baseDir) {
      this.baseDir = baseDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String path = exchange.getRequestURI().getPath();

      // Security check: prevent directory traversal
      if (path.contains("..")) {
        exchange.sendResponseHeaders(403, -1);
        return;
      }

      File file = new File(baseDir, path).getCanonicalFile();
      if (!file.getPath().startsWith(new File(baseDir).getCanonicalPath())) {
        exchange.sendResponseHeaders(403, -1);
        return;
      }

      if (!file.exists() || !file.isFile()) {
        logger.warn("File not found: {}", file.getAbsolutePath());
        exchange.sendResponseHeaders(404, -1);
        return;
      }

      String contentType = Files.probeContentType(file.toPath());
      if (contentType == null) {
        contentType = "application/octet-stream";
      }

      exchange.getResponseHeaders().set("Content-Type", contentType);
      exchange.sendResponseHeaders(200, file.length());

      try (FileInputStream fis = new FileInputStream(file);
          OutputStream os = exchange.getResponseBody()) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
          os.write(buffer, 0, bytesRead);
        }
      }
    }
  }
}
