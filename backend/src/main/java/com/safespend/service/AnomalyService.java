package com.safespend.service;
import com.safespend.domain.Alert;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AnomalyService {
  private static final double K = 1.4826;
  private static final double THRESH = 3.5;

  public Optional<Alert> evaluate(String userId, String category, BigDecimal amount, List<BigDecimal> history) {
    if (history.size() < 8) return Optional.empty();
    List<Double> xs = history.stream().map(BigDecimal::doubleValue).sorted().toList();
    double m = median(xs);
    List<Double> dev = xs.stream().map(v -> Math.abs(v - m)).sorted().toList();
    double mad = median(dev);
    double z = mad == 0 ? 0 : Math.abs(amount.doubleValue() - m) / (K * mad);
    if (z >= THRESH) {
      Alert a = new Alert();
      a.setUserId(userId); a.setCategory(category); a.setAmount(amount);
      a.setMedian(BigDecimal.valueOf(m)); a.setMad(BigDecimal.valueOf(mad)); a.setZScore(z);
      a.setReason(String.format("%.1fx from median in %s", z, category));
      return Optional.of(a);
    }
    return Optional.empty();
  }

  private double median(List<Double> s){
    int n=s.size();
    return (n%2==1)? s.get(n/2): (s.get(n/2-1)+s.get(n/2))/2.0;
  }
}
