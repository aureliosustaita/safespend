package com.safespend.service;

import com.safespend.domain.Transaction;
import com.safespend.repo.AlertRepo;
import com.safespend.repo.TransactionRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TxnIngestService {

  private static final Logger log = LoggerFactory.getLogger(TxnIngestService.class);

  private final TransactionRepo txRepo;
  private final AlertRepo alertRepo;
  private final AnomalyService anomaly;

  // Dev toggle for simulating flaky downstream behavior (default: off)
  @Value("${safespend.ingest.simulate-failure:false}")
  private boolean simulateFailure;

  // Probability used when simulateFailure=true
  @Value("${safespend.ingest.simulate-failure-rate:0.10}")
  private double simulateFailureRate;

  public TxnIngestService(TransactionRepo t, AlertRepo a, AnomalyService an) {
    this.txRepo = t;
    this.alertRepo = a;
    this.anomaly = an;
  }

  /**
   * Retries + Circuit Breaker configured under name "ingest" in application-postgres.yml
   */
  @Timed(value = "txn_ingest", description = "Time taken to ingest a transaction")
  @Transactional
  @Retry(name = "ingest", fallbackMethod = "ingestFallback")
  @CircuitBreaker(name = "ingest", fallbackMethod = "ingestFallback")
  public Transaction ingest(Transaction t) {
    if (t.getTimestamp() == null) {
      t.setTimestamp(Instant.now());
    }

    // OPTIONAL demo failure to visualize retries/CB (enable via property)
    if (simulateFailure && ThreadLocalRandom.current().nextDouble() < simulateFailureRate) {
      throw new RuntimeException("Simulated downstream timeout");
    }

    Transaction saved = txRepo.save(t);
    evaluateAndMaybeAlert(saved);
    return saved;
  }

  /**
   * Fallback when retries are exhausted or the circuit breaker is OPEN.
   * Signature must match original args + a Throwable at the end.
   */
  @Transactional
  @SuppressWarnings("unused") // called reflectively by Resilience4j
  private Transaction ingestFallback(Transaction t, Throwable ex) {
    log.warn("Fallback ingest invoked due to: {}", ex.toString());

    // Mark transaction as pending so the UI can reflect degraded ingestion.
    String m = t.getMerchant();
    t.setMerchant((m == null || m.isBlank() ? "" : m + " ") + "(pending)");

    Transaction saved = txRepo.save(t);
    evaluateAndMaybeAlert(saved);
    return saved;
  }

  // ----- helpers -----

  private void evaluateAndMaybeAlert(Transaction t) {
    // Convert historical BigDecimal amounts -> Double for anomaly service
    List<Double> history = txRepo
        .findTop200ByUserIdAndCategoryOrderByTimestampDesc(t.getUserId(), t.getCategory())
        .stream()
        .map(Transaction::getAmount)   // BigDecimal
        .filter(Objects::nonNull)
        .map(bd -> bd.doubleValue())   // -> Double
        .toList();

    // Current txn amount: BigDecimal -> double
    double amount = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;

    anomaly.evaluate(t.getUserId(), t.getCategory(), amount, history)
        .ifPresent(a -> {
          alertRepo.save(a);
          // 🔔 Log when we actually create an alert
          log.info("ALERT created: user={} cat={} amt={} median={} mad={} z={}",
              a.getUserId(), a.getCategory(), a.getAmount(), a.getMedian(), a.getMad(), a.getZScore());
        });
  }
}
