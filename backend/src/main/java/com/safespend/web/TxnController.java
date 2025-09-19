package com.safespend.web;

import com.safespend.api.TransactionDTO;
import com.safespend.domain.Transaction;
import com.safespend.repo.TransactionRepo;
import com.safespend.service.TxnIngestService;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;   // <-- add this

@RestController
@RequestMapping("/api/transactions")
public class TxnController {

  private final TxnIngestService svc;
  private final TransactionRepo repo;

  public TxnController(TxnIngestService s, TransactionRepo r) {
    this.svc = s;
    this.repo = r;
  }

  @PostMapping
  public Transaction post(@Valid @RequestBody TransactionDTO dto) {
    Transaction t = new Transaction();
    t.setUserId(dto.userId());
    t.setMerchant(dto.merchant());
    t.setCategory(dto.category());
    t.setAmount(BigDecimal.valueOf(dto.amount()));  // <-- convert double -> BigDecimal
    return svc.ingest(t);
  }

  @GetMapping
  public List<Transaction> list() {
    return repo.findAll(PageRequest.of(0, 200, Sort.by("timestamp").descending()))
               .getContent();
  }
}
