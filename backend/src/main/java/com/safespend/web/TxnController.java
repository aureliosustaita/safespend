package com.safespend.web;

import com.safespend.api.TransactionDTO;
import com.safespend.domain.Transaction;
import com.safespend.repo.TransactionRepo;
import com.safespend.service.TxnIngestService;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TxnController {

  private final TxnIngestService svc;
  private final TransactionRepo repo;

  public TxnController(TxnIngestService s, TransactionRepo r) {
    this.svc = s;
    this.repo = r;
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping
  public Transaction post(@Valid @RequestBody TransactionDTO dto, Authentication auth) {
    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    // Non-admins can only post for themselves; ignore any userId they send
    String effectiveUserId = isAdmin && dto.userId() != null && !dto.userId().isBlank()
        ? dto.userId()
        : auth.getName();

    Transaction t = new Transaction();
    t.setUserId(effectiveUserId);
    t.setMerchant(dto.merchant());
    t.setCategory(dto.category());
    t.setAmount(BigDecimal.valueOf(dto.amount()).setScale(2, RoundingMode.HALF_UP));
    return svc.ingest(t);
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping
  public List<Transaction> list(
      @RequestParam(required = false) String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      Authentication auth
  ) {
    // clamp page & size
    page = Math.max(0, page);
    size = Math.min(Math.max(1, size), 200);

    boolean isAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    if (!isAdmin) {
      // Users can only see their own transactions
      userId = auth.getName();
    }

    var pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

    if (userId == null) {
      // Admin listing all users' transactions
      return repo.findAll(pageable).getContent();
    } else {
      return repo.findByUserIdOrderByTimestampDesc(userId, pageable).getContent();
    }
  }
}
