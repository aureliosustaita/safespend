package com.safespend.service;

import com.safespend.domain.Transaction;
import com.safespend.repo.TransactionRepo;
import com.safespend.repo.AlertRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TxnIngestService {

  private static final Logger log = LoggerFactory.getLogger(TxnIngestService.class);

  private final TransactionRepo txRepo;
  private final AlertRepo alertRepo;
  private final AnomalyService anomaly;

  public TxnIngestService(TransactionRepo t, AlertRepo a, AnomalyService an) {
    this.txRepo = t;
    this.alertRepo = a;
    this.anomaly = an;
  }

  /**
   * Retries + Circuit Breaker configured under name "ingest" in application-postgres.yml
   * Example:
   *
   * resilience4j:
   *   retry:
   *     instances:
   *       ingest:
   *         max-attempts: 3
   *         wait-duration: 200ms
   *   circuitbreaker:
   *     instances:
   *       ingest:
   *         sliding-window-type: COUNT_BASED
   *         sliding-window-size: 20
   *         failure-rate-threshold: 50
   *         wait-duration-in-open-state: 10s
   */
  @Timed(value = "txn_ingest", description = "Time taken to ingest a transaction")
  @Transactional
  @Retry(name = "ingest", fallbackMethod = "ingestFallback")
  @CircuitBreaker(name = "ingest", fallbackMethod = "ingestFallback")
  public Transaction ingest(Transaction t) {
    if (t.getTimestamp() == null) {
      t.setTimestamp(Instant.now());
    }

    // OPTIONAL: simulate a flaky downstream (~10%) to demo retries/CB behavior.
    // Remove this block for real prod behavior.
    if (Math.random() < 0.10) {
      throw new RuntimeException("Simulated downstream timeout");
    }

    Transaction saved = txRepo.save(t);

    var history = txRepo
        .findTop200ByUserIdAndCategoryOrderByTimestampDesc(t.getUserId(), t.getCategory())
        .stream()
        .map(Transaction::getAmount)
        .toList();

    anomaly
        .evaluate(t.getUserId(), t.getCategory(), t.getAmount(), history)
        .ifPresent(alertRepo::save);

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
    t.setMerchant(t.getMerchant() + " (pending)");

    Transaction saved = txRepo.save(t);

    var history = txRepo
        .findTop200ByUserIdAndCategoryOrderByTimestampDesc(t.getUserId(), t.getCategory())
        .stream()
        .map(Transaction::getAmount)
        .toList();

    anomaly
        .evaluate(t.getUserId(), t.getCategory(), t.getAmount(), history)
        .ifPresent(alertRepo::save);

    return saved;
  }
}
