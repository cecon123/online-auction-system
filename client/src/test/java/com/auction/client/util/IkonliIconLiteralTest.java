package com.auction.client.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kordamp.ikonli.javafx.FontIcon;

class IkonliIconLiteralTest {

  private static final Pattern ICON_LITERAL_PATTERN = Pattern.compile("iconLiteral=\"([^\"]+)\"");

  @ParameterizedTest
  @MethodSource("iconLiterals")
  void fxmlIconLiteralsResolve(String iconLiteral) {
    assertDoesNotThrow(() -> new FontIcon(iconLiteral));
  }

  private static List<String> iconLiterals() {
    try (var paths = java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources/fxml"))) {
      return paths
          .filter(path -> path.toString().endsWith(".fxml"))
          .flatMap(
              path -> {
                try {
                  String content = java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
                  return ICON_LITERAL_PATTERN
                      .matcher(content)
                      .results()
                      .map(MatchResult::group)
                      .map(match -> match.substring("iconLiteral=\"".length(), match.length() - 1));
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              })
          .distinct()
          .sorted()
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
