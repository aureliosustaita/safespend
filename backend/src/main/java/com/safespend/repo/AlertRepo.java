package com.safespend.repo;
import com.safespend.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlertRepo extends JpaRepository<Alert, Long> {
  List<Alert> findTop100ByUserIdOrderByCreatedAtDesc(String userId);
}
