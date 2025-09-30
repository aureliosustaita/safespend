package com.safespend.web;

import com.safespend.domain.Alert;
import com.safespend.repo.AlertRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
  private final AlertRepo repo;
  public AlertController(AlertRepo r){ this.repo = r; }

  /**
   * ADMIN:
   *   - if userId is provided: returns that user’s alerts
   *   - if userId is missing: returns all alerts
   * USER:
   *   - ignores provided userId and returns ONLY their own alerts
   */
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping
  public List<Alert> list(
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
      // USERs can only see their own alerts (prevents IDOR)
      userId = auth.getName();
    }

    var sort = Sort.by("createdAt").descending();
    var pageable = PageRequest.of(page, size, sort);

    Page<Alert> pageResult;
    if (userId == null) {
      // ADMIN listing all
      pageResult = repo.findAll(pageable);
    } else {
      // by user
      pageResult = repo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    return pageResult.getContent();
  }
}
