package com.safespend.service;

import com.safespend.domain.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(AnomalyService.class);

  // Tunables
  private static final int MIN_HISTORY = 12;
  private static final double Z_THRESHOLD = 3.5;
  private static final double MAD_TO_SIGMA = 1.4826; // sigma ≈ MAD * 1.4826
  private static final double IQR_TO_SIGMA = 1.349;  // sigma ≈ IQR / 1.349

  // Fallback thresholds when MAD==0 and IQR==0 (all history identical)
  private static final double ABS_JUMP_DOLLARS = 20.0; // trigger if abs jump >= $20
  private static final double REL_JUMP_MULTIPLIER = 5.0; // or >= 5x the median

  /**
   * Evaluate a new amount against recent history for a user+category.
   */
  public Optional<Alert> evaluate(String userId, String category, double amount, List<Double> history) {
    if (history == null || history.size() < MIN_HISTORY) {
      log.info("ANOMALY skip: not enough history (have {}, need >= {}) user={} cat={}",
          history == null ? 0 : history.size(), MIN_HISTORY, userId, category);
      return Optional.empty();
    }

    // Copy & sort for robust stats
    List<Double> vals = new ArrayList<>(history);
    Collections.sort(vals);

    double median = median(vals);

    // --- Primary path: MAD-based robust z ---
    List<Double> devs = new ArrayList<>(vals.size());
    for (double v : vals) devs.add(Math.abs(v - median));
    double mad = median(devs);

    if (mad > 0.0) {
      double z = (amount - median) / (mad * MAD_TO_SIGMA);
      double az = Math.abs(z);

      log.info("ANOMALY eval (MAD): user={} cat={} n={} median={} mad={} amount={} z={} thr={}",
          userId, category, vals.size(), median, mad, amount, z, Z_THRESHOLD);

      if (az >= Z_THRESHOLD) {
        return Optional.of(buildAlert(userId, category, amount, median, mad, z,
            String.format("Robust z=%.2f via MAD (median=%.2f, MAD=%.2f, n=%d).",
                z, median, mad, vals.size())));
      }
      log.info("ANOMALY no-alert (MAD): |z|={} < {}", az, Z_THRESHOLD);
      return Optional.empty();
    }

    // --- Fallback 1: IQR-based z if MAD==0 but there is still spread in quartiles ---
    double q1 = percentile(vals, 0.25);
    double q3 = percentile(vals, 0.75);
    double iqr = q3 - q1;

    if (iqr > 0.0) {
      double sigma = iqr / IQR_TO_SIGMA;
      double z = (amount - median) / sigma;
      double az = Math.abs(z);

      log.info("ANOMALY eval (IQR fallback): user={} cat={} n={} median={} q1={} q3={} iqr={} z={} thr={}",
          userId, category, vals.size(), median, q1, q3, iqr, z, Z_THRESHOLD);

      if (az >= Z_THRESHOLD) {
        return Optional.of(buildAlert(userId, category, amount, median, /*mad*/0.0, z,
            String.format("Fallback IQR z=%.2f (median=%.2f, IQR=%.2f, n=%d).",
                z, median, iqr, vals.size())));
      }
      log.info("ANOMALY no-alert (IQR fallback): |z|={} < {}", az, Z_THRESHOLD);
      return Optional.empty();
    }

    // --- Fallback 2: all history identical (MAD==0 and IQR==0). Use jump rules. ---
    double delta = Math.abs(amount - median);
    boolean hugeAbs = delta >= ABS_JUMP_DOLLARS;
    boolean hugeRel = (median > 0.0) && (amount / median >= REL_JUMP_MULTIPLIER);

    log.info("ANOMALY eval (jump fallback): user={} cat={} n={} median={} amount={} Δ={} abs>={}x? {} rel>={}x? {}",
        userId, category, vals.size(), median, amount, delta, ABS_JUMP_DOLLARS, hugeAbs, REL_JUMP_MULTIPLIER, hugeRel);

    if (hugeAbs || hugeRel) {
      double pseudoZ = median > 0 ? (amount - median) / (Math.max(1e-6, median / REL_JUMP_MULTIPLIER))
                                  : Double.POSITIVE_INFINITY;
      return Optional.of(buildAlert(userId, category, amount, median, /*mad*/0.0, pseudoZ,
          String.format("Baseline has no variation; flagged jump (Δ=$%.2f, x%.1f vs median %.2f, n=%d).",
              delta, median > 0 ? amount / median : Double.POSITIVE_INFINITY, median, vals.size())));
    }

    log.info("ANOMALY skip: MAD=0 & IQR=0 and no large jump (amount near constant median={}).", median);
    return Optional.empty();
  }

  // ---- helpers ----

  private static Alert buildAlert(String userId, String category, double amount,
                                  double median, double mad, double z, String extraReason) {
    String direction = (z > 0) ? "above" : "below";
    String reason = String.format(
        "Amount $%.2f is %s the typical level (median=$%.2f). %s",
        amount, direction, median, extraReason
    );

    Alert a = new Alert();
    a.setUserId(userId);
    a.setCategory(category);
    a.setAmount(bd2(amount));
    a.setMedian(bd2(median));
    a.setMad(bd2(mad)); // will be 0.00 if we used a fallback
    a.setZScore(z);
    a.setReason(reason);
    a.setCreatedAt(Instant.now());
    return a;
  }

  private static double median(List<Double> sorted) {
    int n = sorted.size();
    if (n == 0) return 0.0;
    if ((n & 1) == 1) return sorted.get(n / 2);
    return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
  }

  // Linear-interpolated percentile on a sorted list
  private static double percentile(List<Double> sorted, double p) {
    int n = sorted.size();
    if (n == 0) return 0.0;
    if (p <= 0) return sorted.get(0);
    if (p >= 1) return sorted.get(n - 1);
    double idx = p * (n - 1);
    int lo = (int) Math.floor(idx);
    int hi = (int) Math.ceil(idx);
    double frac = idx - lo;
    if (hi == lo) return sorted.get(lo);
    return sorted.get(lo) + frac * (sorted.get(hi) - sorted.get(lo));
  }

  private static BigDecimal bd2(double v) {
    return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
  }
}
