package com.safespend.repo;
import com.safespend.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
  List<Transaction> findTop200ByUserIdAndCategoryOrderByTimestampDesc(String userId, String category);
}
