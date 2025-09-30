package com.safespend.repo;

import com.safespend.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepo extends JpaRepository<Alert, Long> {
  // (optional) keep for any legacy callers
  List<Alert> findTop100ByUserIdOrderByCreatedAtDesc(String userId);

  // used by the updated AlertController with paging
  Page<Alert> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
