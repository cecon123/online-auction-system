package com.auction.client.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

/** Manages the Price History Chart in the Live Bidding view. */
public class PriceChartManager {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
  public static final int MAX_DATA_POINTS = 15;
  private final AreaChart<String, Number> chart;
  private final XYChart.Series<String, Number> series;

  public PriceChartManager(AreaChart<String, Number> chart) {
    this.chart = chart;
    this.series = new XYChart.Series<>();
    this.series.setName("Price History");
    initializeChart();
  }

  private void initializeChart() {
    chart.getData().add(series);
    chart.setAnimated(false); // Important for realtime updates
    chart.setLegendVisible(false);
  }

  public void addPricePoint(BigDecimal price) {
    String timeStr = LocalDateTime.now().format(TIME_FMT);
    addPricePoint(timeStr, price);
  }

  public void addPricePoint(String timeLabel, BigDecimal price) {
    runOnFx(
        () -> {
          series.getData().add(new XYChart.Data<>(timeLabel, price.doubleValue()));
          if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
          }
        });
  }

  public void clear() {
    runOnFx(() -> series.getData().clear());
  }

  public void setData(java.util.List<com.auction.common.dto.bid.PlaceBidResponse> history) {
    runOnFx(
        () -> {
          series.getData().clear();
          // Take the last N points for visibility
          int start = Math.max(0, history.size() - MAX_DATA_POINTS);
          for (int i = start; i < history.size(); i++) {
            com.auction.common.dto.bid.PlaceBidResponse bid = history.get(i);
            series
                .getData()
                .add(
                    new XYChart.Data<>(
                        bid.timestamp().format(TIME_FMT), bid.currentPrice().doubleValue()));
          }
        });
  }

  public void setPoints(java.util.List<BidTimeline.ChartPoint> points) {
    runOnFx(
        () -> {
          series.getData().clear();
          for (BidTimeline.ChartPoint point : points) {
            series.getData().add(new XYChart.Data<>(point.label(), point.amount().doubleValue()));
          }
        });
  }

  private void runOnFx(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      Platform.runLater(action);
    }
  }
}
