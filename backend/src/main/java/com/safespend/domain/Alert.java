package com.safespend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;

  private String category;

  private BigDecimal amount;

  private BigDecimal median;

  private BigDecimal mad;

  private double zScore;

  // Store full JSON reason; text is fine for H2/Postgres
  @Lob
  @Column(columnDefinition = "text")
  private String reason;

  private Instant createdAt = Instant.now();

  public Long getId(){ return id; }
  public String getUserId(){ return userId; }
  public void setUserId(String v){ this.userId=v; }
  public String getCategory(){ return category; }
  public void setCategory(String v){ this.category=v; }
  public BigDecimal getAmount(){ return amount; }
  public void setAmount(BigDecimal v){ this.amount=v; }
  public BigDecimal getMedian(){ return median; }
  public void setMedian(BigDecimal v){ this.median=v; }
  public BigDecimal getMad(){ return mad; }
  public void setMad(BigDecimal v){ this.mad=v; }
  public double getZScore(){ return zScore; }
  public void setZScore(double v){ this.zScore=v; }
  public String getReason(){ return reason; }
  public void setReason(String v){ this.reason=v; }
  public Instant getCreatedAt(){ return createdAt; }
  public void setCreatedAt(Instant v){ this.createdAt=v; }
}
