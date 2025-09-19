package com.safespend.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
public class Transaction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private String userId;
  private String merchant;
  private String category;
  private BigDecimal amount;
  private Instant timestamp;
  public Long getId(){ return id; }
  public String getUserId(){ return userId; }
  public void setUserId(String v){ this.userId=v; }
  public String getMerchant(){ return merchant; }
  public void setMerchant(String v){ this.merchant=v; }
  public String getCategory(){ return category; }
  public void setCategory(String v){ this.category=v; }
  public BigDecimal getAmount(){ return amount; }
  public void setAmount(BigDecimal v){ this.amount=v; }
  public Instant getTimestamp(){ return timestamp; }
  public void setTimestamp(Instant v){ this.timestamp=v; }
}
