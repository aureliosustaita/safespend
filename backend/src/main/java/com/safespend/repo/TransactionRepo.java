package com.safespend.repo;

import com.safespend.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
  // keep: used by anomaly history
  List<Transaction> findTop200ByUserIdAndCategoryOrderByTimestampDesc(String userId, String category);

  // new: used by TxnController GET /api/transactions with paging
  Page<Transaction> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

  // optional: paging by category if you need it later
  Page<Transaction> findByUserIdAndCategoryOrderByTimestampDesc(String userId, String category, Pageable pageable);
}
