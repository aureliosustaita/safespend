package com.safespend.service;

import com.safespend.domain.Transaction;
import com.safespend.repo.TransactionRepo;
import com.safespend.repo.AlertRepo;
import com.safespend.domain.Alert; // ok if unused
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TxnIngestService {
  private final TransactionRepo txRepo;
  private final AlertRepo alertRepo;
  private final AnomalyService anomaly;

  public TxnIngestService(TransactionRepo t, AlertRepo a, AnomalyService an){
    this.txRepo = t;
    this.alertRepo = a;
    this.anomaly = an;
  }

  // Retries + Circuit Breaker configured under name "ingest" in application.yml
  @Retry(name = "ingest", fallbackMethod = "ingestFallback")
  @CircuitBreaker(name = "ingest", fallbackMethod = "ingestFallback")
  public Transaction ingest(Transaction t) {
    if (t.getTimestamp() == null) t.setTimestamp(Instant.now());

    // OPTIONAL: simulate a flaky downstream ~10% to demo retries/CB
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

  // Fallback when retries are exhausted or the breaker is OPEN.
  // Signature must match original args + a Throwable at the end.
  private Transaction ingestFallback(Transaction t, Throwable ex){
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
