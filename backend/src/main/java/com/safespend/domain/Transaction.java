package com.safespend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")   // <-- align with V1__init.sql
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, length = 64)
  private String userId;  // V1 has VARCHAR(64)

  @Column(name = "merchant", nullable = false, length = 128)
  private String merchant;

  @Column(name = "category", nullable = false, length = 64)
  private String category;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  // V1 used column name "timestamp"
  @Column(name = "timestamp", nullable = false)
  private Instant timestamp = Instant.now();

  // getters/setters…
  public Long getId() { return id; }
  public String getUserId() { return userId; }
  public void setUserId(String v) { this.userId = v; }
  public String getMerchant() { return merchant; }
  public void setMerchant(String v) { this.merchant = v; }
  public String getCategory() { return category; }
  public void setCategory(String v) { this.category = v; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal v) { this.amount = v; }
  public Instant getTimestamp() { return timestamp; }
  public void setTimestamp(Instant v) { this.timestamp = v; }
}