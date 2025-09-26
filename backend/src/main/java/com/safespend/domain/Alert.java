package com.safespend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, length = 64)
  private String userId;

  @Column(name = "category", nullable = false, length = 64)
  private String category;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "median", nullable = false, precision = 12, scale = 2)
  private BigDecimal median;

  @Column(name = "mad", nullable = false, precision = 12, scale = 2)
  private BigDecimal mad;

  @Column(name = "z_score", nullable = false)
  private double zScore;

  @Lob
  @Column(name = "reason", nullable = false, columnDefinition = "text")
  private String reason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }

  public Long getId() { return id; }

  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }

  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = scale2(amount); }

  public BigDecimal getMedian() { return median; }
  public void setMedian(BigDecimal median) { this.median = scale2(median); }

  public BigDecimal getMad() { return mad; }
  public void setMad(BigDecimal mad) { this.mad = scale2(mad); }

  public double getZScore() { return zScore; }
  public void setZScore(double zScore) { this.zScore = zScore; }

  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  private static java.math.BigDecimal scale2(java.math.BigDecimal v) {
    if (v == null) return null;
    return v.setScale(2, RoundingMode.HALF_UP);
  }
}
