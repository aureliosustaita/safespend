package com.safespend.service;

import com.safespend.domain.Alert;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AnomalyService {

  private static final int MIN_HISTORY = 12;
  private static final double Z_THRESHOLD = 3.5;
  private static final double MAD_TO_SIGMA = 1.4826;

  public Optional<Alert> evaluate(String userId, String category, double amount, List<Double> history) {
    if (history == null || history.size() < MIN_HISTORY) return Optional.empty();

    List<Double> vals = new ArrayList<>(history);
    Collections.sort(vals);

    double median = median(vals);
    List<Double> devs = new ArrayList<>(vals.size());
    for (double v : vals) devs.add(Math.abs(v - median));
    double mad = median(devs);
    if (mad == 0.0) return Optional.empty();

    double z = (amount - median) / (mad * MAD_TO_SIGMA);
    double az = Math.abs(z);

    if (az >= Z_THRESHOLD) {
      String direction = (z > 0) ? "above" : "below";
      String reason = String.format(
          "Amount $%.2f is %.1fx MAD %s the median $%.2f for %s (n=%d, MAD=%.2f).",
          amount, az, direction, median, category, vals.size(), mad
      );

      Alert a = new Alert();
      a.setUserId(userId);
      a.setCategory(category);
      a.setAmount(bd2(amount));
      a.setMedian(bd2(median));
      a.setMad(bd2(mad));
      a.setZScore(z);
      a.setReason(reason);
      a.setCreatedAt(Instant.now());
      return Optional.of(a);
    }

    return Optional.empty();
  }

  private static double median(List<Double> sorted) {
    int n = sorted.size();
    if (n == 0) return 0.0;
    if ((n & 1) == 1) return sorted.get(n / 2);
    return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
  }

  private static BigDecimal bd2(double v) {
    return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
  }
}
